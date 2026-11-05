# Local Jenkins controller with the tools the Jenkinsfile needs: Maven, Docker CLI, kubectl, gitleaks
FROM jenkins/jenkins:lts-jdk17
USER root
COPY --from=docker:27-cli /usr/local/bin/docker /usr/local/bin/docker
RUN apt-get update && apt-get install -y --no-install-recommends maven curl \
 && rm -rf /var/lib/apt/lists/* \
 && curl -fsSLo /usr/local/bin/kubectl "https://dl.k8s.io/release/v1.31.2/bin/linux/amd64/kubectl" \
 && chmod +x /usr/local/bin/kubectl \
 && curl -fsSL https://github.com/gitleaks/gitleaks/releases/download/v8.21.2/gitleaks_8.21.2_linux_x64.tar.gz \
    | tar -xz -C /usr/local/bin gitleaks
RUN jenkins-plugin-cli --plugins workflow-aggregator git credentials-binding junit timestamper
# Lab setup: skip the first-run wizard. Jenkins is bound to localhost only (see docker-compose.yml).
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"
