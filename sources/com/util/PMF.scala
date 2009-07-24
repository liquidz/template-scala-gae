package com.util

import javax.jdo.{JDOHelper, PersistenceManager, PersistenceManagerFactory}

object PMF {
	var pmfInstance:PersistenceManagerFactory =
		JDOHelper.getPersistenceManagerFactory("transactions-optional")

	def withManager(f:PersistenceManager => Unit):Unit = {
		var pm = pmfInstance.getPersistenceManager
		try {
			f(pm)
		} finally {
			pm.close
		}
	}
}
