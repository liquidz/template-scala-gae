package com.view

import scala.xml._

object MainPage {
	val postForm = <form method="get" action="/new">
<label for="firstName">First Name</label>
<input type="text" name="firstName" />
<label for="lastName">Last Name</label>
<input type="text" name="lastName" />
<input type="submit" name="" />
</form>

	def html[A](x:Map[Symbol,A]):Node = {
		Layout.html(
<div id="container">
<h1>Hello {x apply'nickname} with method {x apply'method}</h1>
<p>path was {x apply'path}</p>
<p>{x apply'count} names were found:</p>
<div>{x apply'people}</div>
<a href={(x apply'logout).toString}>logout</a>
{postForm}
</div>
		)
	}
}
