// Requires the Kubernetes plugin, and Jenkins running in-cluster under a
// service account allowed to create pods (see cicd namespace setup) and to
// manage deployments/services/secrets/configmaps/pvcs in the "local"
// namespace (see helm/spring-bill chart).
pipeline {

    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: kaniko
    image: gcr.io/kaniko-project/executor:debug
    command:
    - sleep
    args:
    - infinity
    volumeMounts:
    - name: workspace-volume
      mountPath: /workspace
  - name: helm
    image: alpine/helm:3.15.4
    command:
    - sleep
    args:
    - infinity
    volumeMounts:
    - name: workspace-volume
      mountPath: /workspace
'''
        }
    }

    environment {
        REGISTRY  = "docker-registry.registry.svc.cluster.local:5000"
        IMAGE     = "spring-bill"
        NAMESPACE = "local"
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    triggers {
        githubPush()
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & push image') {
            steps {
                container('kaniko') {
                    sh '''
                    /kaniko/executor \
                      --context=$(pwd) \
                      --dockerfile=$(pwd)/Dockerfile \
                      --destination=$REGISTRY/$IMAGE:$BUILD_NUMBER \
                      --destination=$REGISTRY/$IMAGE:latest \
                      --insecure \
                      --skip-tls-verify
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                container('helm') {
                    sh '''
                    helm upgrade --install $IMAGE helm/spring-bill \
                      -n $NAMESPACE \
                      --set image.tag=$BUILD_NUMBER
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "Pushed ${REGISTRY}/${IMAGE}:${BUILD_NUMBER} and :latest, deployed to ${NAMESPACE}"
        }
    }
}