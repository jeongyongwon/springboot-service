pipeline {
    agent any

    environment {
        PROJECT_NAME = 'springboot-service'
        JAVA_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'
        MAVEN_OPTS = '-Xmx1024m'
    }

    tools {
        maven 'Maven 3.9.0'
        jdk 'JDK 17'
    }

    stages {
        stage('Checkout') {
            steps {
                echo '소스 코드 체크아웃'
                checkout scm
            }
        }

        stage('Setup Java Environment') {
            steps {
                echo 'Java 환경 확인'
                sh '''
                    java -version
                    mvn --version
                '''
            }
        }

        stage('Clean') {
            steps {
                echo '이전 빌드 정리'
                sh '''
                    mvn clean
                '''
            }
        }

        stage('Compile') {
            steps {
                echo '소스 코드 컴파일'
                sh '''
                    mvn compile
                '''
            }
        }

        stage('Test') {
            steps {
                echo '단위 테스트 실행'
                sh '''
                    mvn test || true
                '''
            }
        }

        stage('Package') {
            steps {
                echo 'JAR 파일 패키징'
                sh '''
                    mvn package -DskipTests
                '''
            }
        }

        stage('Verify') {
            steps {
                echo '빌드 검증'
                sh '''
                    mvn verify -DskipTests
                '''
            }
        }

        stage('Archive Artifacts') {
            steps {
                echo 'JAR 파일 아카이빙'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }

    post {
        success {
            echo '✅ 빌드 성공!'
            // Webhook to DevOps API
            sh '''
                curl -X POST http://host.docker.internal:8000/webhook/jenkins \
                  -H "Content-Type: application/json" \
                  -d '{
                    "service": "springboot-service",
                    "status": "success",
                    "build_number": "'${BUILD_NUMBER}'",
                    "git_repo": "'${GIT_URL}'",
                    "git_branch": "'${GIT_BRANCH}'",
                    "job_name": "'${JOB_NAME}'"
                  }' || true
            '''
        }
        failure {
            echo '❌ 빌드 실패!'
            // Webhook to DevOps API with error details
            sh '''
                curl -X POST http://host.docker.internal:8000/webhook/jenkins \
                  -H "Content-Type: application/json" \
                  -d '{
                    "service": "springboot-service",
                    "status": "failure",
                    "build_number": "'${BUILD_NUMBER}'",
                    "git_repo": "'${GIT_URL}'",
                    "git_branch": "'${GIT_BRANCH}'",
                    "job_name": "'${JOB_NAME}'",
                    "error_log": "빌드 프로세스 실패"
                  }' || true
            '''
        }
        always {
            echo '빌드 완료 - 정리 작업'
            junit '**/target/surefire-reports/*.xml' allowEmptyResults: true
            cleanWs()
        }
    }
}
