package com.example.demo.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.common.FlashData;
import com.example.demo.entity.Event;
import com.example.demo.service.EventService;

@Controller
@RequestMapping("/")
public class EventsController {
	@Autowired
	EventService eventService;

	/*
	 * 一覧表示
	 */
	@GetMapping(path = {"", "/"})
	public String list(Model model) {
		// 全件取得
		List<Event> events = eventService.findAll();
		model.addAttribute("events", events);
		return "events/list";
	}
	
	/*
	 * 受注表示
	 */
	@GetMapping(value = "events/view/{id}")
	public String view(@PathVariable Integer id, Model model, RedirectAttributes ra) {
		try {
			// 存在確認
			Event event = eventService.findById(id);
			model.addAttribute("event", event);
		} catch (Exception e) {
			FlashData flash = new FlashData().danger("該当データがありません");
			ra.addFlashAttribute("flash", flash);
			return "redirect:/";
		}
		return "events/view";
	}
}
