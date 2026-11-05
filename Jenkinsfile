// Jenkins version of the same CI/CD pipeline (runs on the local Jenkins in jenkins/).
// Credentials expected in Jenkins: acr-server (secret text), acr-creds (username/password), kubeconfig (secret file)
pipeline {
    agent any

    triggers {
        pollSCM('H/2 * * * *')   // check GitHub for new commits every 2 minutes
    }

    environment {
        ACR_SERVER = credentials('acr-server')
        IMAGE_NAME = 'cia-app'
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script { env.SHORT_SHA = sh(script: 'git rev-parse --short=7 HEAD', returnStdout: true).trim() }
            }
        }

        stage('Compile') {
            steps { sh 'mvn -B clean compile' }
        }

        stage('Unit Tests') {
            steps { sh 'mvn -B test' }
            post { always { junit 'target/surefire-reports/*.xml' } }
        }

        stage('Package') {
            steps { sh 'mvn -B package -DskipTests' }
        }

        stage('Secret Scan (Gitleaks)') {
            steps { sh 'gitleaks detect --source . --no-banner --redact' }
        }

        stage('Docker Build & Tag') {
            steps {
                sh 'docker build --build-arg BUILD_SHA=$SHORT_SHA -t $ACR_SERVER/$IMAGE_NAME:jenkins-$SHORT_SHA .'
            }
        }

        stage('Push to ACR') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'acr-creds', usernameVariable: 'ACR_USERNAME', passwordVariable: 'ACR_PASSWORD')]) {
                    sh '''
                        echo "$ACR_PASSWORD" | docker login $ACR_SERVER -u "$ACR_USERNAME" --password-stdin
                        docker push $ACR_SERVER/$IMAGE_NAME:jenkins-$SHORT_SHA
                    '''
                }
            }
        }

        stage('Deploy to AKS') {
            steps {
                withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG')]) {
                    sh '''
                        sed "s|IMAGE_PLACEHOLDER|$ACR_SERVER/$IMAGE_NAME:jenkins-$SHORT_SHA|" k8s/deployment.yaml > /tmp/deployment.yaml
                        kubectl apply -f /tmp/deployment.yaml -f k8s/service.yaml
                        kubectl rollout status deployment/cia-app --timeout=240s
                    '''
                }
            }
        }

        stage('Verify') {
            steps {
                withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG')]) {
                    sh '''
                        kubectl get pods -l app=cia-app -o wide
                        IP=$(kubectl get svc cia-app-svc -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
                        curl -fsS http://$IP/health
                        echo "App URL: http://$IP"
                    '''
                }
            }
        }
    }

    post {
        success { echo 'Pipeline succeeded: new version is live on AKS.' }
        failure { echo 'Pipeline failed - check the stage logs above.' }
    }
}
