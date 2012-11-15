package geb.spock

import geb.*
import spock.lang.*

abstract class MultiBrowserGebSpec extends Specification {
	String gebConfEnv = null
	String gebConfScript = null

	// Map of geb browsers which can be referenced by name in the spec
	// THese currently share the same config.  This is not a problem for
	// my uses, but I can see potential for wanting to configure different
	// browsers separately
	@Shared _browsers = createBrowserMap()
	def currentBrowser

	def createBrowserMap() {
		[:].withDefault { new Browser(createConf()) }
	}

	Configuration createConf() {
		// Use the standard configured geb driver, but turn off cacheing so
		// we can run multiple
		def conf = new ConfigurationLoader(gebConfEnv).getConf(gebConfScript)
		conf.cacheDriver = false
		return conf
	}

	boolean withBrowserSession(browser, Closure c) {
		currentBrowser = browser
		c.call()
		currentBrowser = null
	}

	void resetBrowsers() {
		_browsers.each { k, browser ->
			if (browser.config?.autoClearCookies) {
				browser.clearCookiesQuietly()
			}
			browser.close()
		}
		_browsers = createBrowserMap()
	}

	def propertyMissing(String name) {
		if(currentBrowser) {
			return currentBrowser."$name"
		} else {
			return _browsers[name]
		}
	}

	def methodMissing(String name, args) {
		if(currentBrowser) {
			return currentBrowser."$name"(*args)
		} else {
			def browser = _browsers[name]
			if(args) {
				return browser."${args[0]}"(*(args[1..-1]))
			} else {
				return browser
			}
		}
	}

	def propertyMissing(String name, value) {
		if(!currentBrowser) throw new IllegalArgumentException("No context for setting property $name")
		currentBrowser."$name" = value
	}

	private isSpecStepwise() {
		this.class.getAnnotation(Stepwise) != null
	}

	def cleanup() {
		if (!isSpecStepwise()) resetBrowsers()
	}

	def cleanupSpec() {
		if (isSpecStepwise()) resetBrowsers()
	}
}

