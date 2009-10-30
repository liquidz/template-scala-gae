package com.util

import javax.jdo.{Extent, JDOHelper, PersistenceManager, PersistenceManagerFactory}

object PMF {
	val pmfInstance:PersistenceManagerFactory =
		JDOHelper.getPersistenceManagerFactory("transactions-optional")

	
	def withManager(f:PersistenceManager => Any):Any = {
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

	def deleteIf[A](k:Class[A])(f:A => Boolean):Unit = withManager(pm => {
		foreach(pm, k)(item => {
			if(f(item)) pm.deletePersistent(item)
		})
	})

	def get[A, B](k:Class[A], id:B):A = withManager(pm => {
		pm.getObjectById(k, id)
	}).asInstanceOf[A]

	def insert[A](obj:A):Unit = withManager(pm => {
		pm.makePersistent(obj)
	})

	def update[A](obj:A) = insert(obj)

	// ====
	//select(classOf[Person], List("firstName == ", params(":name")))
	// ====

	//def select[A](k:Class[A], filter:List[String], params:List[String], q:Object):List[A] = {
	def select[A](k:Class[A], filterList:List[Object]*):List[A] = {
		var result:java.util.List[A] = null
		PMF.withManager(pm => {
			//var map = new java.util.HashMap[String, Object]()
			var qa:List[Object] = List()
			val query = pm.newQuery(k)
			filterList.foreach(ls => {
				if(ls.length == 3){
					query.setFilter(ls(0).asInstanceOf[String])
					query.declareParameters(ls(1).asInstanceOf[String])
					//map.put(ls(1).asInstanceOf[String], ls(2))
					qa = ls(2) :: qa
				}
			})
		
			try {
				//result = query.execute(q).asInstanceOf[java.util.List[A]]
				result = query.executeWithArray(qa.reverse.toArray).asInstanceOf[java.util.List[A]]
				//result = query.executeWithMap(map).asInstanceOf[java.util.List[A]]
				pm.detachCopyAll(result)
			} finally {
				query.closeAll
			}
		})

		javaList2scalaList(result)
	}

	private def javaList2scalaList[A](ls:java.util.List[A]):List[A] = {
		var res:List[A] = List()
		for(val i <- 0 until ls.size){
			res = ls.get(i).asInstanceOf[A] :: res
		}
		res.reverse
	}
}
