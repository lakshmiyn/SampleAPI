node('docker') {
  properties(
    [buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '20', daysToKeepStr: '', numToKeepStr: '100')),
    pipelineTriggers([cron('H 6 * * 1-5')])]
  );

  stage('Retrieve source code') {
    checkout scm
    checkout([$class: 'GitSCM', branches: [[name: "*/master"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'delivery']], submoduleCfg: [], userRemoteConfigs: [[url: 'git@github.skillsoft.com:pints/delivery.git']]])
  }

  def delivery = load 'delivery/delivery.groovy'

  stage('Test') {
    print "${params.CONTEXT}"
    def environment = delivery.environmentsByBranch()[env.BRANCH_NAME]
    if (environment == null) {
      // Default to 'dev' if branch name isn't listed in delivery/delivery.groovy
      //
      // We couldan't use delivery.environmentsByBranch().get(env.BRANCH_NAME, 'dev') above
      // because of Jenkins' security sandbox.
      environment = 'dev'
    }
    try {
      /*docker.image('maven:3.5-jdk-8-alpine').inside {
           sh "mvn -Dserver.port=443 -Dserver.environment=${environment} clean install"
          } */
          docker.image('markhobson/maven-chrome').inside {
            sh "mvn -Dserver.port=443 -Dserver.environment=${environment} clean install"
          }
      delivery.notify('good', "(<${env.BUILD_URL}|Job>) `${JOB_NAME}:${env.BUILD_NUMBER}`\nTests passed.")
    }
    catch (e) {
      currentBuild.result = "FAILED"
      delivery.notifyFailure(e.getMessage())
      throw e
    }
    finally {
      junit 'target/surefire-reports/*.xml'
    }
  }
}
