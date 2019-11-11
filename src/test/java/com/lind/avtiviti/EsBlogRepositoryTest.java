package com.lind.avtiviti;

import com.lind.avtiviti.es.EsBlog;
import com.lind.avtiviti.es.EsBlogRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EsBlogRepositoryTest {

  @Autowired
  private EsBlogRepository esBlogRepository;

  @Before
  public void initRepositoryData() {

    //清除所有的数据
    esBlogRepository.deleteAll();
    //初始化数据
    esBlogRepository.save(new EsBlog(
        "computer",
        "it device",
        "Most great product"));
    esBlogRepository.save(new EsBlog(
        "mobile",
        "call device",
        "Fastest development product"));
    esBlogRepository.save(new EsBlog(
        "note pc",
        "it device",
        "中国最伟大的发明"));
  }

  @Test
  public void testFindDistinctEsBlogByTitleContainingOrSummaryContainingOrContentContaining() {
    Pageable pageable = PageRequest.of(0, 20);

    String title = "computer";
    String summary = "call";
    String content = "发明";

    Page<EsBlog> page = esBlogRepository.findByTitleContainingOrSummaryContainingOrContentContaining(title, summary, content, pageable);
    System.out.println("------------start 1");
    for(EsBlog blog : page) {
      System.out.println(blog.toString());
    }
    System.out.println("------------end 1");
  }

}