class ExampleSpec extends MultiBrowserReportingGebSpec {
	def 'test that project permissions are updated without requiring restart'() {
		given: 'archie and bob are logged in, and archie has a project'
			withBrowserSession a, {
				loginAs('archie', 'secret')
				createNewProject('X')
			}
                        withBrowserSession b, {
				loginAs('bob', 'password')
				to Dashboard
				assert projectCount == 0
			}

		when: 'archie grants bob access to his project'
			withBrowserSession a, {
				grantProjectAccess('X', 'bob')
			}

		then: 'bob can see the project in his project list'
			b.to Dashboard
			b.projectCount == 1

		when: 'bob tries to view the project'
			b.projects[0].click()

		then: 'bob can access the project'
			b.at ProjectXInbox
	}
}

