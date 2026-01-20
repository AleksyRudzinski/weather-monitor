package pl.edu.pjatk.weathermonitor.web.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pl.edu.pjatk.weathermonitor.repository.RefreshJobItemRepository;
import pl.edu.pjatk.weathermonitor.repository.RefreshJobRepository;

import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
public class JobsViewController {

    private final RefreshJobRepository refreshJobRepository;
    private final RefreshJobItemRepository refreshJobItemRepository;

    public JobsViewController(
            RefreshJobRepository refreshJobRepository,
            RefreshJobItemRepository refreshJobItemRepository
    ) {
        this.refreshJobRepository = refreshJobRepository;
        this.refreshJobItemRepository = refreshJobItemRepository;
    }

    @GetMapping("/ui/jobs")
    public String jobs(Model model) {
        var jobs = refreshJobRepository.findTop20ByOrderByStartTimeDesc();

        var jobIds = jobs.stream().map(j -> j.getId()).toList();
        var items = jobIds.isEmpty()
                ? java.util.List.<pl.edu.pjatk.weathermonitor.domain.RefreshJobItem>of()
                : refreshJobItemRepository.findAllForJobs(jobIds);

        // grupowanie itemów per jobId
        var itemsByJobId = items.stream().collect(Collectors.groupingBy(i -> i.getJob().getId()));

        // policz success/failed per job
        var successCount = new HashMap<Long, Integer>();
        var failedCount = new HashMap<Long, Integer>();

        for (var job : jobs) {
            var list = itemsByJobId.getOrDefault(job.getId(), java.util.List.of());
            int ok = 0, fail = 0;
            for (var it : list) {
                if ("SUCCESS".equalsIgnoreCase(it.getStatus())) ok++;
                if ("FAILED".equalsIgnoreCase(it.getStatus())) fail++;
            }
            successCount.put(job.getId(), ok);
            failedCount.put(job.getId(), fail);
        }

        model.addAttribute("jobs", jobs);
        model.addAttribute("itemsByJobId", itemsByJobId);
        model.addAttribute("successCount", successCount);
        model.addAttribute("failedCount", failedCount);

        return "jobs";
    }
}
