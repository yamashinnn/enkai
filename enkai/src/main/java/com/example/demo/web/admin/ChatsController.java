package com.example.demo.web.admin;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.common.DataNotFoundException;
import com.example.demo.common.FlashData;
import com.example.demo.entity.Chat;
import com.example.demo.entity.Event;
import com.example.demo.entity.EventUser;
import com.example.demo.entity.User;
import com.example.demo.service.ChatService;
import com.example.demo.service.EventService;
import com.example.demo.service.EventUserService;
import com.example.demo.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/chats")
public class ChatsController {
	@Autowired
	ChatService chatService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	EventUserService eventUserService;
	
	@Autowired
	EventService eventService;

	/*
	 * 一覧表示
	 */
	@GetMapping(value = "/talk/{id}")
	public String talk(@PathVariable Integer id, Model model, RedirectAttributes ra) {
		FlashData flash;
		// SpringSecurity側からログインユーザの情報を取得する
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		List<EventUser> eventUsers = eventUserService.findByEventId(id);
		List<String> eventUserList = eventUsers.stream().map(s -> s.getUser().getEmail()).collect(Collectors.toList());
		if (eventUserList.contains(email)) {
			// 全件取得
			List<Chat> chats = chatService.findByEventId(id);
			model.addAttribute("chats", chats);
			Event event = eventUsers.stream().findFirst().get().getEvent();
			Chat chat = new Chat();
			chat.setEvent(event);
			User user;
			try {
				user = userService.findByEmail(email);
			} catch (DataNotFoundException e) {
				flash = new FlashData().danger("該当データがありません");
				ra.addFlashAttribute("flash", flash);
				return "redirect:/admin/events/view/" + id;
			}
			chat.setUser(user);
			model.addAttribute("chat", chat);
		} else {
			flash = new FlashData().danger("イベントに参加していません");
			ra.addFlashAttribute("flash", flash);
			return "redirect:/admin/events/view/" + id;
		}
		return "admin/chats/talk";
	}

	/*
	 * 新規登録
	 */
	@PostMapping(value = "/create")
	public String register(@Valid Chat chat, BindingResult result, Model model, RedirectAttributes ra) {
		FlashData flash;
		Integer id = chat.getEvent().getId();
		try {
			if (result.hasErrors()) {
				return "redirect:/admin/chats/talk/" + id;
			}
			// 存在確認
			eventService.findById(id);
			
			// SpringSecurity側からログインユーザの情報を取得する
			String email = SecurityContextHolder.getContext().getAuthentication().getName();
			List<EventUser> eventUsers = eventUserService.findByEventId(id);
			List<String> eventUserList = eventUsers.stream().map(s -> s.getUser().getEmail()).collect(Collectors.toList());
			if (eventUserList.contains(email)) {
				// 新規登録
				chatService.save(chat);
				flash = new FlashData().success("新規作成しました");
			} else {
				flash = new FlashData().danger("イベントに参加していません");
				ra.addFlashAttribute("flash", flash);
				return "redirect:/admin/";
			}
		} catch (DataNotFoundException e) {
			flash = new FlashData().danger("該当データがありません");
			ra.addFlashAttribute("flash", flash);
			return "redirect:/admin/";
		} catch (Exception e) {
			flash = new FlashData().danger("処理中にエラーが発生しました");
		}
		ra.addFlashAttribute("flash", flash);
		return "redirect:/admin/chats/talk/" + id;
	}
}
