import javax.jdo.Extent
import javax.jdo.annotations._
import javax.servlet.http._
import scala.xml._

import com.util.{Auth, PMF}
import com.model.Person

import com.thinkminimo.step._
import com.uo.liquidz.SimpleView._

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
		val p = new Person(params("firstName"), params("lastName"))
		PMF.withManager(pm => {
			pm.makePersistent(p)
		})
		template.layout("ok", <h2>{p.toString}</h2>);
	}

	get("/list"){
		/*
		var people:Node = <ul></ul>
		PMF.foreach(classOf[Person])(p => {
			people = add(people, <li>{p.toString}</li>)
		})
		template.layout("list", <div>{people}</div>)
		*/

		import java.util.List
		var people:java.util.List[Person] = null
		var res:Node = <div></div>
		PMF.withManager(pm => {
			val query = "select from " + classOf[Person].getName
			people = pm.newQuery(query).execute.asInstanceOf[java.util.List[Person]]
			val ite = people.iterator
			while(ite.hasNext) res = add(res, <div>{(ite.next.asInstanceOf[Person]).toString}</div>)
		})
		template.layout("list", <h2>{ res }</h2>)
	}

	get("/del/:str"){
		var target:String = ""
		PMF.deleteIf(classOf[Person])(p => {
			if(p.firstName == params(":str")){
				target = p.toString
				true
			} else {
				false
			}
		})

		if(target != ""){
			template.layout("delete", <h2>done: { target }</h2>)
		} else {
			template.layout("delete", <h2>target is not found</h2>)
		}
	}

	get("/date/:year/:month/:day"){
		<html><head><title>aaa</title></head>
			<body>
				<h1>date</h1>
				<p>{params(":year")} / {params(":month")} / {params(":day")}</p>
			</body>
		</html>
	}

	private def add(p:Node, newEntry:Node):Node = p match {
		case <div>{ ch @ _* }</div> => <div>{ ch }{ newEntry }</div>
		case <ul>{ ch @ _* }</ul> => <ul>{ ch }{ newEntry }</ul>
	}

}

/*
import com.view._

class StartServlet extends HttpServlet {
	var _req:HttpServletRequest = null
	var _resp:HttpServletResponse = null

	override def doGet(request:HttpServletRequest, response:HttpServletResponse):Unit = {
		if(Auth.isNotLogined){
			response.sendRedirect(Auth.loginURL(request))
		} else {
			_req = request
			_resp = response

			request.getPathInfo match {
				case "/new" => newPerson
				case _ => main
			}
		}
	}

	// =main
	// -------------------------------
	private def main():Unit = {
		var count = 0
		var people:Node = <div class="people"></div>

		PMF.foreach(classOf[Person])(p => {
			count += 1
			people = add(people, <div class="person">{p.firstName} {p.lastName}</div>)
		})

		MainPage.nickname = Auth.user.getNickname
		MainPage.method = _req.getMethod
		MainPage.path = _req.getPathInfo
		MainPage.count = count
		MainPage.people = people
		MainPage.logout = Auth.logoutURL(_req)

		_resp.getWriter().println(MainPage.html)
	}

	// =newPerson
	// -------------------------------
	private def newPerson():Unit = {
		PMF.withManager(pm => {
			val p = new Person(_req.getParameter( "firstName"), _req.getParameter( "lastName"))
			pm.makePersistent(p)
		})
		_resp.sendRedirect("/")
	}

	// =add
	// -------------------------------
	private def add(p:Node, newEntry:Node):Node = p match {
		case <div>{ ch @ _* }</div> => <div>{ ch }{ newEntry }</div>
	}
}
*/
