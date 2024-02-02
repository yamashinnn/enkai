package com.example.demo.web.admin;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.common.DataNotFoundException;
import com.example.demo.common.FlashData;
import com.example.demo.entity.Event;
import com.example.demo.entity.EventUser;
import com.example.demo.entity.User;
import com.example.demo.service.EventService;
import com.example.demo.service.EventUserService;
import com.example.demo.service.UserService;

@Controller
@RequestMapping("/admin/eventusers")
public class EventUsersController {
	@Autowired
	EventService eventService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	EventUserService eventUserService;

	/*
	 * イベント参加
	 */
	@GetMapping(value = "/create/{id}")
	public String register(@PathVariable Integer id, RedirectAttributes ra) {
		FlashData flash;
		try {
			// SpringSecurity側からログインユーザの情報を取得する
			String email = SecurityContextHolder.getContext().getAuthentication().getName();
			List<EventUser> eventUsers = eventUserService.findByEventId(id);
			List<String> eventUserList = eventUsers.stream().map(s -> s.getUser().getEmail()).collect(Collectors.toList());
			// ログインユーザがイベントに参加している場合、「イベント表示画面」へリダイレクト
			if (eventUserList.contains(email)) {
				flash = new FlashData().danger("既に参加しています");
				ra.addFlashAttribute("flash", flash);
				return "redirect:/admin/events/view/" + id;
			};
			
			User user;
			Event event;
			try {
				user = userService.findByEmail(email);
				event = eventService.findById(id);
			} catch (DataNotFoundException e) {
				flash = new FlashData().danger("該当データがありません");
				ra.addFlashAttribute("flash", flash);
				return "redirect:/admin/events/view/" + id;
			}
			
			// イベントユーザの登録数が「最大参加者数」に到達している場合、「イベント表示画面」へリダイレクト
			if (event.getMaxParticipant() <= eventUsers.size()) {
				flash = new FlashData().danger("最大参加者数に到達しています");
				ra.addFlashAttribute("flash", flash);
				return "redirect:/admin/events/view/" + id;
			}
			
			EventUser eventUser = new EventUser();
			eventUser.setUser(user);
			eventUser.setEvent(event);
			
			eventUserService.save(eventUser);
			flash = new FlashData().success("新規作成しました");
		} catch (Exception e) {
			flash = new FlashData().danger("処理中にエラーが発生しました");
		}
		ra.addFlashAttribute("flash", flash);
		return "redirect:/admin/events/view/" + id;
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
			List<EventUser> eventUsers = eventUserService.findByEventId(id);
			List<String> eventUserList = eventUsers.stream().map(s -> s.getUser().getEmail()).collect(Collectors.toList());
			if (eventUserList.contains(email)) {
				Integer eventUserId = eventUsers.stream().filter(s-> s.getUser().getEmail().equals(email)).findFirst().get().getId();
				eventUserService.deleteById(eventUserId);
				flash = new FlashData().success("イベントの辞退が完了しました");
			} else {
				flash = new FlashData().danger("イベントに参加していません");
			}
		} catch (Exception e) {
			flash = new FlashData().danger("処理中にエラーが発生しました");
		}
		ra.addFlashAttribute("flash", flash);
		return "redirect:/admin/events/view/" + id;
	}
}
