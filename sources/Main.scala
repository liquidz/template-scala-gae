import javax.jdo.Extent
import javax.jdo.annotations._
import javax.servlet.http._
import scala.xml._

import com.util.{Auth, PMF}
import com.view._
import com.model.Thing

class StartServlet extends HttpServlet {
	override def doGet(request:HttpServletRequest, response:HttpServletResponse):Unit = {

		if(Auth.isNotLogined){
			response.sendRedirect(Auth.loginURL(request))
			return
		}

		var count = 0
		var people:Node = <div class="people"></div>

		if(request.getPathInfo == "/new"){
			PMF.withManager(pm => {
				val t = new Thing(request.getParameter( "firstName"), request.getParameter( "lastName"))
				pm.makePersistent(t)
			})
			response.sendRedirect("/")
		}   

		PMF.withManager(pm => {
			val extent:Extent[Thing] = pm.getExtent(classOf[Thing], false)
			val i = extent.iterator
			while(i.hasNext) {
				count += 1
				val person = i.next
				people = add(people, <div class="person">{person.firstName} {person.lastName}</div>)
			}
			extent.closeAll()
		})

		response.getWriter().println(
			MainPage.html(Map(
				'nickname -> Auth.user.getNickname,
				'method -> request.getMethod,
				'path -> request.getPathInfo,
				'count -> count,
				'people -> people,
				'logout -> Auth.logoutURL(request)
				)
			)
		)
	}

	private def add(p:Node, newEntry:Node):Node = p match {
		case <div>{ ch @ _* }</div> => <div>{ ch }{ newEntry }</div>
	}
}
