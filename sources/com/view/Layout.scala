package com.view

import scala.xml._

object Layout {
	def html[A](body:A):Node =
<html>
<head>
	<title>Hello, world2</title>
</head>
<body>{body}</body>
</html>
}
