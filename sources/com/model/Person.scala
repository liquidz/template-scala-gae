package com.model

import javax.jdo.annotations._

//@PersistenceCapable {val identityType = IdentityType.APPLICATION}
@PersistenceCapable { val identityType = IdentityType.APPLICATION, val detachable = "true" }
class Person(@Persistent var firstName:String, @Persistent var lastName :String){
	@PrimaryKey
	@Persistent {val valueStrategy = IdGeneratorStrategy.IDENTITY}
	var id: java.lang.Long = null 

	override def toString():String = "[Person: " + id + ", " + firstName + " " + lastName + "]"
}
