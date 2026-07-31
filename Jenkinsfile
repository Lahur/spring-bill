// Requires the Kubernetes plugin, and Jenkins running in-cluster under a
// service account allowed to create pods (see cicd namespace setup).
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
'''
        }
    }

    environment {
        REGISTRY = "docker-registry.registry.svc.cluster.local:5000"
        IMAGE    = "spring-bill"
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
                      --skip-tls-verify \
                      --cache=true
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "Pushed ${REGISTRY}/${IMAGE}:${BUILD_NUMBER} and :latest"
        }
    }
}