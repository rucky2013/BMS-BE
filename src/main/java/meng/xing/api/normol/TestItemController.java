package meng.xing.api.normol;

import meng.xing.entity.Subject;
import meng.xing.entity.TestItem;
import meng.xing.entity.testItem.TestItemType;
import meng.xing.service.SubjectService;
import meng.xing.service.TestItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/testItems")
public class TestItemController {
    @Autowired
    private TestItemService testItemService;
    @Autowired
    private SubjectService subjectService;
    @GetMapping
    public Page<TestItem> getAllTestItems(
            @RequestParam(value = "type", defaultValue = "1") int type,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "sort", defaultValue = "id") String sort,
            @RequestParam(value = "order", defaultValue = "asc") String order,
            @RequestParam(value = "subject", defaultValue = "") String subject
    ) {
        Subject subjectObj =null;
        if(subject !="")
        {
            subjectObj = subjectService.findSubjectByType(subject);
        }
        String typeStr;
        if (type == 1) {
            typeStr = TestItemType.QUESTION.toString();
        } else {
            typeStr = TestItemType.CHOICE.toString();
        }
        Sort _sort = new Sort(Sort.Direction.fromString(order), sort);
        //传来的页码是从1开始，而服务器从1开始算
        Pageable pageable = new PageRequest(page - 1, pageSize, _sort);
        return testItemService.findTestItemsByTypeAndSubject(typeStr, subjectObj,pageable);

    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public boolean update(@PathVariable("id") Long id, @RequestBody Map<String, Object> map) {
        TestItem testItem = testItemService.findTestItemById(id);
        testItem.setAnswer(map.get("answer").toString());
        testItem.setQuestion(map.get("question").toString());
        testItem.setSubject(subjectService.findSubjectByType(map.get("subject").toString()));
        return testItemService.updateTestItem(testItem);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public boolean create(@RequestBody Map<String, Object> map) {
        TestItem testItem = new TestItem(
                map.get("type").toString(),
                map.get("question").toString(),
                map.get("answer").toString());
        testItem.setSubject(subjectService.findSubjectByType(map.get("subject").toString()));
        return testItemService.addTestItme(testItem);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public boolean delete(@PathVariable("id") Long id) {
        return testItemService.deleteTestItemById(id);
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@RequestBody Map<String, ArrayList<Long>> map) {
        map.get("ids").forEach(id -> testItemService.deleteTestItemById(id));
    }

}
