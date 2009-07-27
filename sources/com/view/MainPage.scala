package com.view

import scala.xml._

object MainPage extends Layout {
	var (nickname, method, path, logout) = ("", "", "", "")
	var count = 0
	var people:Node = null

	val postForm = <form method="get" action="/new">
<label for="firstName">First Name</label>
<input type="text" name="firstName" />
<label for="lastName">Last Name</label>
<input type="text" name="lastName" />
<input type="submit" name="" />
</form>

	override def container:Any = 
<h1>Hello, {nickname} with method {method}</h1>
<p>path was {path}</p>
<p>{count} names were found:</p>
<div>{people}</div>
<a href={logout}>logout</a>
<div>{postForm}</div>
}
