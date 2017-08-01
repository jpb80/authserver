package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value="/user")
public class UserController {

	
	@RequestMapping(method=RequestMethod.GET, value="/getUser")
	public String getUser() {
		return "test";
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/setUser")
	public String setUser() {
		return "saved";
	}
}
