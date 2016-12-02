node {
    def tests = [:]
    for(int i = 0; i < 5; i++) {
        def z = 'run' + i
        tests[z] = {stage('run' + i) { echo 'Passed!' } }
    }
    parallel tests
}