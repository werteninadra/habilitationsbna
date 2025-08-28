pipeline {
    agent any

    environment {
        MAVEN_OPTS = '-Dmaven.test.skip=false'
    }

    stages {
        stage('Checkout Code') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/nadra-wertani/habilitationsbna.git'
            }
        }

        stage('Build Backend') {
            steps {
                dir('habilitationbna') {
                    sh 'mvn clean compile'
                }
            }
        }

        stage('Run Tests Backend') {
            steps {
                dir('habilitationbna') {
                    sh 'mvn test'
                }
            }
        }

        stage('JaCoCo Coverage') {
            steps {
                dir('habilitationbna') {
                    sh 'mvn jacoco:report'
                }
                jacoco execPattern: '**/target/jacoco.exec', classPattern: '**/target/classes', sourcePattern: '**/src/main/java'
            }
        }

        stage('SonarQube Analysis') {
            environment {
                SONAR_HOST_URL = 'http://localhost:9000'
                SONAR_LOGIN = credentials('sonar-token')
            }
            steps {
                dir('habilitationbna') {
                    sh '''
                        mvn sonar:sonar \
                          -Dsonar.projectKey=habilitationbna \
                          -Dsonar.projectName="habilitationbna" \
                          -Dsonar.host.url=$SONAR_HOST_URL \
                          -Dsonar.login=$SONAR_LOGIN
                    '''
                }
            }
        }

        
        stage('Deploy to Nexus') {
            steps {
                dir('habilitationbna') {
                    sh 'mvn deploy -Dmaven.test.skip=true'
                }
            }
        }
    
    }

   
}
