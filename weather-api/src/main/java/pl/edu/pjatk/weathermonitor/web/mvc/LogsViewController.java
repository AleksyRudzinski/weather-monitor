package pl.edu.pjatk.weathermonitor.web.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.edu.pjatk.weathermonitor.web.rest.LogsRestController;

@Controller
public class LogsViewController {

    private final LogsRestController logsRestController;

    public LogsViewController(LogsRestController logsRestController) {
        this.logsRestController = logsRestController;
    }

    @GetMapping("/ui/logs")
    public String logs(@RequestParam(defaultValue = "200") int lines, Model model) throws Exception {
        String content = logsRestController.getLogs(lines);
        model.addAttribute("lines", lines);
        model.addAttribute("content", content);
        return "logs";
    }

    @PostMapping("/ui/logs/clear")
    public String clearLogs(
            @RequestParam(defaultValue = "200") int lines,
            RedirectAttributes ra
    ) throws Exception {
        logsRestController.clearLogs();
        ra.addFlashAttribute("toastSuccess", "Wyczyszczono logi.");
        return "redirect:/ui/logs?lines=" + lines;
    }
}
