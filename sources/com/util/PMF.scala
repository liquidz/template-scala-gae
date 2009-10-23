package com.util

import javax.jdo.{Extent, JDOHelper, PersistenceManager, PersistenceManagerFactory}

object PMF {
	val pmfInstance:PersistenceManagerFactory =
		JDOHelper.getPersistenceManagerFactory("transactions-optional")

	def withManager(f:PersistenceManager => Unit):Unit = {
		val pm = pmfInstance.getPersistenceManager
		try {
			f(pm)
		} finally {
			pm.close
		}
	}

	def foreach[A](k:Class[A])(f:A => Unit):Unit = {
		withManager(pm => foreach(pm, k)(f))
	}

	def foreach[A](pm:PersistenceManager, k:Class[A])(f:A => Unit):Unit = {
		val extent:Extent[A] = pm.getExtent(k, false)
		val i = extent.iterator
		while(i.hasNext) f(i.next)
		extent.closeAll
	}

	def deleteIf[A](k:Class[A])(f:A => Boolean):Unit = {
		withManager(pm => {
			foreach(pm, k)(item => {
				if(f(item)) pm.deletePersistent(item)
			})
		})
	}
}
