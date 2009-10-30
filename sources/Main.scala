import javax.jdo.Extent
import javax.jdo.annotations._
import javax.servlet.http._
import scala.xml._

import com.thinkminimo.step._

import com.util._
import com.util.ExtendedEntity._

object template {
	def layout(title:String, content:Elem) = <html>
		<head><title>{ title }</title></head>
		<body>
			<h1>{ title}</h1>
			{ content }
		</body>
	</html>
}

class StartServlet extends Step {
	get("/"){
		template.layout("hello", <form method="POST" action="/post">
			<input type="text" name="firstName" />
			<input type="text" name="lastName" />
			<input type="submit" value="send" />
		</form>)
	}

	post("/post"){
		val e = new ExtendedEntity("Person")
		e.set("firstName", params("firstName")).set("lastName", params("lastName")).store

		template.layout("ok", <h2>{e}</h2>)
	}

	get("/list"){
		var res:Node = <ul></ul>
		ExtendedEntity.select("Person").foreach(me => {
			res = add(res, <li>{me.getId}: {me("firstName")}, {me("lastName")}</li>)
		})

		template.layout("list", <div>{res}</div>)
	}

	get("/get/:id"){
		val e = ExtendedEntity.get("Person", params(":id").toLong)
		if(e != null) template.layout("get", <div>{e}</div>)
		else template.layout("get", <p>not found</p>)
	}

	get("/select/:str"){
		var res:Node = <div></div>

		ExtendedEntity.select("Person", List("firstName == ?", params(":str"))).foreach(e => {
			res = add(res, <p>{e("firstName")}, {e("lastName")}</p>)
		})

		template.layout("select", <div>{res}</div>)


	}

	get("/select2/:str/:str2") {
		var res:Node = <div></div>

		ExtendedEntity.select("Person", List("firstName == ? and lastName == ?", params(":str"), params(":str2"))).foreach(e => {
			res = add(res, <p>{e("firstName")}, {e("lastName")}</p>)
		})

		template.layout("select2", <div>{res}</div>)
	}

	get("/edit/:id"){
		val en = ExtendedEntity.get("Person", params(":id").toLong)
		if(en != null){
			template.layout("edit", <form method="POST" action="/edit">
				<input type="hidden" name="id" value={params(":id")} />
				<p><input type="text" name="firstName" value={en("firstName").toString} /></p>
				<p><input type="text" name="lastName" value={en("lastName").toString} /></p>
				<p><input type="submit" value="update" /></p>
			</form>)
		} else {
			template.layout("edit", <h2>not found</h2>)
		}
	}
	post("/edit"){
		val id = params("id")
		if(id != null){
			val en = ExtendedEntity.get("Person", id.toLong)
			en.set("firstName", params("firstName")).set("lastName", params("lastName")).store
		}

		template.layout("edit", <h2>updated</h2>)
	}

	get("/del/:str"){
		val target = ExtendedEntity.deleteIf("Person", List("firstName == ?", params(":str"))).toString

		if(target != ""){
			template.layout("delete", <h2>done: { target }</h2>)
		} else {
			template.layout("delete", <h2>target is not found</h2>)
		}
	}

	private def add(p:Node, newEntry:Node):Node = p match {
		case <div>{ ch @ _* }</div> => <div>{ ch }{ newEntry }</div>
		case <ul>{ ch @ _* }</ul> => <ul>{ ch }{ newEntry }</ul>
	}
}

