package com.manuelpedreira.shorturl.services;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.manuelpedreira.shorturl.entities.Url;

@Service
public class MetadataExtractorService {

  private static final Logger logger = LoggerFactory.getLogger(MetadataExtractorService.class);

  public Url enrichUrlWithMetaDataJsoup(Url url) throws IOException {

    Document doc = Jsoup.connect(url.getOriginalUrl()).get();

    url.setTitle(doc.title());
    if (url.getTitle().isEmpty())
      url.setTitle(getFirstJsoupSelect(doc,
          "meta[property=og:title]",
          "meta[name=twitter:title]"));

    url.setDescription(getFirstJsoupSelect(doc,
        "meta[name=description]",
        "meta[property=og:description]",
        "meta[name=twitter:description]"));

    url.setImageUrl(getFirstJsoupSelect(doc,
        "meta[property=og:image]",
        "meta[property=og:image:url]",
        "meta[name=twitter:image]",
        "meta[name=image]"));

    return url;
  }

  private String getFirstJsoupSelect(Document doc, String... metas) {
    Integer count = 0;

    for (String meta : metas) {
      String data = doc.select(meta).attr("content");
      count++;
      if (data != null && !data.isEmpty()) {
        logger.info("found at try " + count + " ! ->" + meta + " -> " + data);
        return data;
      }
    }
    return "";
  }

}
