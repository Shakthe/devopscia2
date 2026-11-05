# CIA 2 – End-to-End CI/CD Pipeline on Microsoft Azure

A small Java (Spring Boot) web app that is automatically built, tested, containerised and deployed to
**Azure Kubernetes Service (AKS)** whenever code is pushed to `main`.

```
 git push ──► GitHub Actions / Jenkins
               1. Checkout
               2. Maven compile ─► unit tests ─► package JAR   (+ SonarCloud, Gitleaks)
               3. Docker build & tag (<acr>/cia-app:<git-sha>)
               4. Push image ─► Azure Container Registry (ACR)
               5. Ansible playbook ─► kubectl apply ─► rolling update on AKS
               6. Verify: rollout status, pods Running, HTTP /health = UP
                                   │
                    Browser ──► Azure Load Balancer (public IP) ──► 2 pods on AKS
```

## Repository layout
| Path | Purpose |
|------|---------|
| `src/main/java` | Spring Boot app – `/` (web page) and `/health` (JSON) |
| `src/test/java` | JUnit 5 + MockMvc unit tests |
| `pom.xml` | Maven build (JAR, tests, JaCoCo coverage, SonarCloud settings) |
| `Dockerfile`, `.dockerignore` | Runtime image (JRE 17 Alpine, non-root user) |
| `k8s/` | Deployment (2 replicas, probes, rolling update) and LoadBalancer Service |
| `ansible/` | Inventory + playbook that deploys to AKS and verifies the endpoint |
| `.github/workflows/ci-cd.yml` | GitHub Actions pipeline |
| `Jenkinsfile`, `jenkins/` | Same pipeline on Jenkins (local Docker controller) |
| `infra/azure-setup.sh` | Azure CLI script that creates the RG, ACR and AKS |

## Run locally
```bash
mvn clean package          # compile + test + package
java -jar target/app.jar   # http://localhost:8080
```

## Pipeline secrets (GitHub → Settings → Secrets → Actions)
`ACR_LOGIN_SERVER`, `ACR_USERNAME`, `ACR_PASSWORD`, `KUBE_CONFIG` (base64 kubeconfig), `SONAR_TOKEN` (optional).

## Jenkins
```bash
cd jenkins && docker compose up -d --build   # UI: http://localhost:8081
```
Add credentials `acr-server`, `acr-creds`, `kubeconfig`, then create a *Pipeline from SCM* job pointing at this repo.

## Demo a change
Edit `app.version` / `app.message` in `src/main/resources/application.properties`, commit and push –
the pipeline redeploys and the new version appears at the service URL.

## Clean up
`az group delete -n rg-cia2 --yes --no-wait`
