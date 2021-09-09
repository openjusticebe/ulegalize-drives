package com.ulegalize.controller.dpa;

import com.ulegalize.domain.dto.Message;
import com.ulegalize.service.dpa.DPAService;
import com.ulegalize.service.exception.RestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URL;

@RestController
@Slf4j
@RequestMapping("/dpa")
public class DPAResourceREST {

  @Value("${app.lawfirm.callback.dpa}")
  String URL_CALLBACK;
//  @Autowired
  private DPAService dpaService;

  @GetMapping("/start")
//	@PreAuthorize("hasRole('USER')")
  public ResponseEntity<?> associateDpa() {
//		LawfirmToken lawfirmToken = (LawfirmToken) SecurityContextHolder.getContext().getAuthentication();

    URL url = null;
    try {
      url = dpaService.startToken("lawfirmToken.getVcKey()");
//			url = dpaService.startToken(lawfirmToken.getVcKey());
    } catch (RestException e) {
      log.error("Error while associateDropbox", e);
    }
    return ResponseEntity.ok(url);
  }

  @GetMapping("/finish")
//    @PreAuthorize("hasRole('USER')")
  public RedirectView finish(@RequestParam String code) {
    String token = null;
      try {
          token = dpaService.finishToken(code);

      } catch (RestException e) {
          log.error("Error while finishing dropbox folder", e);
      }

      return new RedirectView(URL_CALLBACK + token);
  }

    @GetMapping("/user")
    public ResponseEntity<?> user() {
        return ResponseEntity.ok(new Message("Content for user"));
    }

    @GetMapping("/resource/admin")
    public ResponseEntity<?> admin() {
        return ResponseEntity.ok(new Message("Content for admin"));
    }

    @GetMapping("/resource/user-or-admin")
	public ResponseEntity<?> userOrAdmin() {
		return ResponseEntity.ok(new Message("Content for user or admin"));
	}
}
