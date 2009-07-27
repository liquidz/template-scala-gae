package com.view

import scala.xml._

trait ViewTemplate {
	var title = "template"
	def head = <head>{title}</head>
	def body = <body></body>
	def html = <html>{head}{body}</html>
}

class Layout extends AnyRef with ViewTemplate {
	title = "hello, world"
	def container:Any = null

	override def body =
<body>
	<div id="container">{container}</div>
</body>
}
