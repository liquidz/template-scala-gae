package com.util

import com.google.appengine.api.users.{User, UserService, UserServiceFactory}
import javax.servlet.http._

object Auth {
	val userService = UserServiceFactory.getUserService
	val user = userService.getCurrentUser()

	def loginURL(req:HttpServletRequest):String = userService.createLoginURL(req.getRequestURI)
	def logoutURL(req:HttpServletRequest):String = userService.createLogoutURL(req.getRequestURI)

	def isLogined = if(user == null) false else true
	def isNotLogined = if(user == null) true else false
}
