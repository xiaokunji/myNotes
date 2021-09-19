# 介绍:
该代码支持:
1. 填充work中的占位符(仅是填充,不能动态加格式之类)
2. 将work转化为html

# 使用
**pom.xml**
```
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>${poi.version}</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml-schemas</artifactId>
    <version>${poi.version}</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>ooxml-schemas</artifactId>
    <version>1.4</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-scratchpad</artifactId>
    <version>${poi.version}</version>
</dependency>
<dependency>
    <groupId>fr.opensagres.xdocreport</groupId>
    <artifactId>xdocreport</artifactId>
    <version>1.0.6</version>
</dependency>
```

**wordUtils**
```
public static HWPFDocument replaceTables2003(String filePath, Map<String, Object> map) throws Exception {

        //logger.info("替换word2003文档表格内容，文档路径:{}，要替换的内容:{}", filePath, JsonUtil.toJSONString(map));

        if (StringUtils.isBlank(filePath) || MapUtils.isEmpty(map)) {
            return null;
        }

        try (FileInputStream is = new FileInputStream(filePath)) {

            HWPFDocument document = new HWPFDocument(is);

            Range range = document.getRange();

            for (Entry<String, Object> e : map.entrySet()) {
                String value = null == e.getValue() ? "" : (String) e.getValue();
                range.replaceText(e.getKey(), value);
            }

            return document;

        } catch (Exception e) {
            logger.error("替换word2003文档表格内容失败:" + e.getMessage(), e);
            throw e;
        }
    }
    
 public static XWPFDocument replaceTables2007(String filePath, Map<String, Object> map) throws Exception {

        //logger.info("替换word2007文档表格内容，文档路径:{}，要替换的内容:{}", filePath, JsonUtil.toJSONString(map));

        if (StringUtils.isBlank(filePath) || MapUtils.isEmpty(map)) {
            return null;
        }

        XWPFDocument document = new XWPFDocument(POIXMLDocument.openPackage(filePath));

     /* 替换段落中的指定文字 */
        Iterator<XWPFParagraph> itPara = document.getParagraphsIterator();
        while (itPara.hasNext()) {
            XWPFParagraph paragraph = itPara.next();
            Set<String> set = map.keySet();
            for (String key : set) {
                List<XWPFRun> run = paragraph.getRuns();
                for (XWPFRun xwpfRun : run) {
                    if (xwpfRun.getText(xwpfRun.getTextPosition()) != null
                            && xwpfRun.getText(xwpfRun.getTextPosition()).contains(key)) {
                        //参数0表示生成的文字是要从哪一个地方开始放置,设置文字从位置0开始 就可以把原来的文字全部替换掉了
                        String text = xwpfRun.getText(xwpfRun.getTextPosition());
                        String value = null == map.get(key) ? "" : (String) map.get(key);
                        if (key.contains("#") || key.contains("$")) {
                            text = text.replaceAll(Pattern.quote(key), value);
                        } else {
                            text = text.replaceAll(key, value);
                        }
                        xwpfRun.setText(text, 0);
                    }
                }
            }
        }
        
        // 表格内容
        Iterator<XWPFTable> it = document.getTablesIterator();
        while (it.hasNext()) {
            XWPFTable table = it.next();
            int rcount = table.getNumberOfRows();
            for (int i = 0; i < rcount; i++) {
                XWPFTableRow row = table.getRow(i);
                List<XWPFTableCell> cells = row.getTableCells();
                for (XWPFTableCell cell : cells) {
                    for (Entry<String, Object> e : map.entrySet()) {
                        if (cell.getText().equals(e.getKey())) {
                            // 删除原来内容
                            cell.removeParagraph(0);
                            if (e.getValue() != null) {
                                // 写入新内容
                                cell.setText((String) e.getValue());
                            }
                        }
                    }
                }
            }
        }
    return document;
    
 }
 
 public static String docToHtml(String wordPath, String fileName) {

        logger.info("将word2003转换成html，word文件路径:{}，word文件名:{}", wordPath, fileName);

        InputStream input = null;
        ByteArrayOutputStream outStream = null;

        try {
            input = new FileInputStream(wordPath + File.separator + fileName);
            // 初始化转换器
            WordToHtmlConverter converter = new WordToHtmlConverter(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());

            // 保存文档中的图片
            converter.setPicturesManager((content, pictureType, suggestedName, widthInches, heightInches) -> {
                // 设定图片路径
                // return srcPath + File.separator + suggestedName;
                return suggestedName;
            });
            // word2003
            HWPFDocument wordDocument = new HWPFDocument(input);
            converter.processDocument(wordDocument);
            // 设定图片
            List<Picture> pics = wordDocument.getPicturesTable().getAllPictures();
            if (CollectionUtils.isNotEmpty(pics)) {
                // 存储图片，根据给定的名称
                for (Picture pic : pics) {
                    // 将文件直接写出去
                    pic.writeImageContent(new FileOutputStream(new File(wordPath, pic.suggestFullFileName())));
                }
            }
            Document docHtml = converter.getDocument();
            DOMSource domSource = new DOMSource(docHtml);

            outStream = new ByteArrayOutputStream();
            StreamResult streamResult = new StreamResult(outStream);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, Constant.FILE_ENCODING);
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty(OutputKeys.METHOD, "html");
            serializer.transform(domSource, streamResult);
            
             // 文件名前缀
            String prefix = CommonUtils.getFilePrefix(fileName);
            String htmlPath = wordPath + File.separator + prefix + ".html";
            logger.info("将word2003转换成html，html路径为:{}", htmlPath);
            File target = new File(htmlPath);
            FileUtils.writeStringToFile(target, new String(outStream.toByteArray()), Constant.FILE_ENCODING);
            logger.info("将word2003转换成html---成功");
            return htmlPath;
        } catch (Exception e) {
            logger.error("将word2003转换成html失败:" + e.getMessage(), e);
            throw new RuntimeException("失败");
        } finally {
            IOUtils.closeOutputStream(outStream);
            IOUtils.closeInputStream(input);
        }

}


public static String docxToHtml(String wordPath, String fileName) {

        logger.info("将word2007转换成html，word文件路径:{}，word文件名:{}", wordPath, fileName);

        OutputStreamWriter writer = null;
        FileInputStream fis = null;

        try {
            String sourceFileName = wordPath + File.separator + fileName;
            // 文件名前缀
            String prefix = CommonUtils.getFilePrefix(fileName);
            String targetFileName = wordPath + File.separator + prefix + ".html";
            logger.info("将word2007转换成html，html路径为:{}", targetFileName);

            // 图片存放路径
            String imagePathStr = wordPath + "/image/";
            fis = new FileInputStream(sourceFileName);
            XWPFDocument document = new XWPFDocument(fis);
            XHTMLOptions options = XHTMLOptions.create();
            File imgFile = new File(imagePathStr);
            if (!imgFile.exists()) {
                imgFile.mkdirs();
            }
            // 存放图片的文件夹
            options.setExtractor(new FileImageExtractor(imgFile));
            // html中图片的路径
            options.URIResolver(new BasicURIResolver("image"));
            options.setIgnoreStylesIfUnused(false);
            options.setFragment(true);

            writer = new OutputStreamWriter(new FileOutputStream(targetFileName), Constant.FILE_ENCODING);
            XHTMLConverter xhtmlConverter = (XHTMLConverter) XHTMLConverter.getInstance();
            xhtmlConverter.convert(document, writer, options);
            logger.info("将word2007转换成html---成功");

            return targetFileName;
             } catch (Exception e) {
            logger.error("将word2007转换成html失败:" + e.getMessage(), e);
            throw new RuntimeException("失败");
        } finally {
            IOUtils.closeWriter(writer);
            IOUtils.closeInputStream(fis);
        }


        
}

 
```