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
import com.example.demo.entity.Event;
import com.example.demo.entity.EventUser;
import com.example.demo.service.EventService;
import com.example.demo.service.EventUserService;
import com.example.demo.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/events")
public class AdminEventsController {
	@Autowired
	EventService eventService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	EventUserService eventUserService;

	/*
	 * 一覧表示
	 */
	@GetMapping(path = {"", "/mylist"})
	public String mylist(Model model, RedirectAttributes ra) {
		// SpringSecurity側からログインユーザの情報を取得する
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		Integer userId;
		try {
			userId = userService.findByEmail(email).getId();
		} catch (DataNotFoundException e) {
			FlashData flash = new FlashData().danger("該当データがありません");
			ra.addFlashAttribute("flash", flash);
			return "redirect:/admin";
		}
		// ログインユーザが登録ユーザであるイベント一覧取得
		List<Event> events = eventService.findByUserId(userId);
		model.addAttribute("events", events);
		return "admin/events/mylist";
	}
	
	/*
	 * イベント表示
	 */
	@GetMapping(value = "/view/{id}")
	public String view(@PathVariable Integer id, Model model, RedirectAttributes ra) {
		try {
			Event event = eventService.findById(id);
			model.addAttribute("event", event);
			
			List<EventUser> eventUsers = eventUserService.findByEventId(id);
			model.addAttribute("eventUsers", eventUsers);
			
			String email = SecurityContextHolder.getContext().getAuthentication().getName();
			List<String> eventUserList = eventUsers.stream().map(s -> s.getUser().getEmail()).collect(Collectors.toList());
			Boolean isParticipation = eventUserList.contains(email);
			model.addAttribute("isParticipation", isParticipation);
		} catch (Exception e) {
			FlashData flash = new FlashData().danger("該当データがありません");
			ra.addFlashAttribute("flash", flash);
			return "redirect:/admin/events/mylist";
		}
		return "admin/events/view";
	}
	
	/*
	 * 新規作成画面表示
	 */
	@GetMapping(value = "/create")
	public String form(Event event, Model model) {
		model.addAttribute("event", event);
		return "admin/events/create";
	}

	/*
	 * 新規登録
	 */
	@PostMapping(value = "/create")
	public String register(@Valid Event event, BindingResult result, Model model, RedirectAttributes ra) {
		FlashData flash;
		try {
			if (result.hasErrors()) {
				return "admin/events/create";
			}
			// 新規登録
			eventService.save(event);
			flash = new FlashData().success("新規作成しました");
		} catch (Exception e) {
			flash = new FlashData().danger("処理中にエラーが発生しました");
		}
		ra.addFlashAttribute("flash", flash);
		return "redirect:/admin/events/mylist";
	}
	
	/*
	 * 編集画面表示
	 */
	@GetMapping(value = "/edit/{id}")
	public String edit(@PathVariable Integer id, Model model, RedirectAttributes ra) {
		try {
			// 存在確認
			Event event = eventService.findById(id);
			model.addAttribute("event", event);
		} catch (Exception e) {
			FlashData flash = new FlashData().danger("該当データがありません");
			ra.addFlashAttribute("flash", flash);
			return "redirect:/admin/events/mylist";
		}
		return "admin/events/edit";
	}

	/*
	 * 更新
	 */
	@PostMapping(value = "/edit/{id}")
	public String update(@PathVariable Integer id, @Valid Event event, BindingResult result, Model model, RedirectAttributes ra) {
		FlashData flash;
		try {
			if (result.hasErrors()) {
				return "admin/events/edit";
			}
			eventService.findById(id);
			// 更新
			eventService.save(event);
			flash = new FlashData().success("更新しました");
		} catch (Exception e) {
			flash = new FlashData().danger("該当データがありません");
		}
		ra.addFlashAttribute("flash", flash);
		return "redirect:/admin/events/mylist";
	}
	
	/*
	 * 削除
	 */
	@GetMapping("/delete/{id}")
	public String delete(@PathVariable Integer id, RedirectAttributes ra) {
		FlashData flash;
		try {
			// SpringSecurity側からログインユーザの情報を取得する
			String email = SecurityContextHolder.getContext().getAuthentication().getName();
			Integer userId = userService.findByEmail(email).getId();
			Integer eventUserId = eventService.findById(id).getUser().getId();
			if (!eventUserId.equals(userId)) {
				flash = new FlashData().success("登録ユーザでなければ削除できません");
			} else {
				List<EventUser> eventUsers = eventUserService.findByEventId(id);
				if (eventUsers.isEmpty()) {
					eventService.deleteById(id);
					flash = new FlashData().success("イベントの削除が完了しました");
				} else {
					flash = new FlashData().danger("イベント参加者がいるイベントは削除できません");
				}
			}
		} catch (DataNotFoundException e) {
			flash = new FlashData().danger("該当データがありません");
		} catch (Exception e) {
			flash = new FlashData().danger("処理中にエラーが発生しました");
		}
		ra.addFlashAttribute("flash", flash);
		return "redirect:/admin/events/mylist";
	}
}
