import javax.jdo.Extent
import javax.jdo.annotations._
import javax.servlet.http._
import scala.xml._

import com.util.{Auth, PMF}
import com.view._
import com.model.Person

class StartServlet extends HttpServlet {
	var _req:HttpServletRequest = null
	var _resp:HttpServletResponse = null

	override def doGet(request:HttpServletRequest, response:HttpServletResponse):Unit = {
		if(Auth.isNotLogined){
			response.sendRedirect(Auth.loginURL(request))
			return
		}

		_req = request
		_resp = response

		request.getPathInfo match {
			case "/new" => newPerson
			case _ => main
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

		_resp.getWriter().println(
			MainPage.html(Map(
				'nickname -> Auth.user.getNickname,
				'method -> _req.getMethod,
				'path -> _req.getPathInfo,
				'count -> count,
				'people -> people,
				'logout -> Auth.logoutURL(_req)
				)
			)
		)
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
