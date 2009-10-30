package com.util

import com.google.appengine.api.datastore._

object ExtendedEntity {
	val ds = DatastoreServiceFactory.getDatastoreService

	// =get
	def get(key:Key):ExtendedEntity = try {
		new ExtendedEntity(ds.get(key))
	} catch {
		case e:Exception => null
	}
	def get(kind:String, id:Long):ExtendedEntity = this.get(KeyFactory.createKey(kind, id))

	// =select
	def select(kind:String):List[ExtendedEntity] = select(kind, null)
	def select(kind:String, filter:List[String]):List[ExtendedEntity] = {
		val query = new Query(kind)
		if(filter != null){
			insertParams(filter).flatMap(parseConditions).map(parseFilter).foreach(f => f match {
				case List(prop, cond, value) => {
					val op = string2filterOperator(cond)
					if(op != null) query.addFilter(prop, op, string2object(value))
				}
				case _ => null
			})
		}
		javaIterator2scalaList(ds.prepare(query).asIterator).map(e => new ExtendedEntity(e))
	}

	// =deleteIf
	def deleteIf(kind:String, filter:List[String]):List[Long] = {
		var ids = List[Long]()
		this.select(kind, filter).foreach(e => {
			ids = e.getId :: ids
			e.delete
		})
		ids.reverse
	}

	// =javaIterator2scalaList
	private def javaIterator2scalaList[A](ite:java.util.Iterator[A]):List[A] = {
		var ls = List[A]()
		while(ite.hasNext){
			ls = ite.next :: ls
		}
		ls.reverse
	}

	// =parseFilter
	private def parseFilter(str:String):List[String] = {
		"""(.+?)(==|[><]=?)(.+?)""".r.unapplySeq(str) match {
			case Some(List(prop, cond, value)) => {
				List(prop.toString.trim, cond.toString.trim, value.toString.trim)
			}
			case None => List()
		}
	}
	
	// =parseConditions
	private def parseConditions(str:String):List[String] = {
		"""(.+?)and(.+?)""".r.unapplySeq(str) match {
			case Some(List(c1, c2)) => {
				List(c1.toString.trim, c2.toString.trim)
			}
			case None => List(str.trim)
		}
	}

	// =insertParams
	private def insertParams(ls:List[Any]):List[String] = {
		if(ls.length > 1){
			var base = ls(0).asInstanceOf[String]
			val reg = """\?""".r
	
			ls.tail.foreach(a => {
				base = reg.replaceFirstIn(base, a.toString)
			})
			List(base)
		} else List()
	}

	// =string2filterOperator
	private def string2filterOperator(s:String):Query.FilterOperator = s match {
		case "==" => Query.FilterOperator.EQUAL
		case ">" => Query.FilterOperator.GREATER_THAN
		case ">=" => Query.FilterOperator.GREATER_THAN_OR_EQUAL
		case "<" => Query.FilterOperator.LESS_THAN
		case "<=" => Query.FilterOperator.LESS_THAN_OR_EQUAL
		case _ => null
	}

	// =string2object
	private def string2object(s:String):Object = {
		"""^[0-9]+$""".r.findFirstIn(s) match {
			case Some(x) => new Integer(Integer.parseInt(x))
			case None => s
		}
	}

	implicit def entity2extendedEntity(e:Entity):ExtendedEntity = new ExtendedEntity(e)
}


class ExtendedEntity(val en:Entity){
	val _ds = ExtendedEntity.ds
	private var entity:Entity = en

	def this(kind:String) = this(new Entity(kind))
	
	// =apply
	def apply(key:String):Any = if(entity.hasProperty(key)) DataConverter.obj2any(entity.getProperty(key)) else null
	def apply(key:String, value:Any):ExtendedEntity = {
		entity.setProperty(key, DataConverter.any2obj(value))
		this
	}

	// =set
	def set(key:String, value:Any) = apply(key, value)

	// =-=
	def -=(key:String):Unit = entity.removeProperty(key)
	// =store
	def store():Key = _ds.put(entity)

	// =delete
	def delete():Unit = {
		_ds.delete(entity.getKey)
		this.entity = null
	}

	// =getId
	def getId():Long = entity.getKey.getId
}


object DataConverter {
	def any2obj(a:Any):Object = a match {
		case ls:List[Object] => {
			// scala list -> java arrayList
			val arr = new java.util.ArrayList[Object](ls.length)
			for(val i <- 0 until ls.length){
				arr.set(i, ls(i))
			}
			arr
		}
		case map:Map[String, Any] => {
			// scala map -> java stack
			val st = new java.util.Stack[Object]()
			map.keys.foreach(key => {
				st.push(any2obj(map(key)))
				st.push(key)
			})
			st
		}
		case Some(x) => any2obj(x)
		case o:Object => o
		case _ => null
	}
	
	def obj2any(o:Object):Any = o match {
		case arr:java.util.ArrayList[Object] => {
			var ls = List[Object]()
			val ite = arr.iterator
			while(ite.hasNext){
				ls = ite.next :: ls
			}
			ls.reverse
		}
		case st:java.util.Stack[Object] => {
			val map = scala.collection.mutable.Map[String, Object]()
			while(!st.empty && st.size >= 2){
				val key = st.pop
				val value = st.pop
				map(key.toString) = value
			}
			map
		}
		case _ => o
	}
}
