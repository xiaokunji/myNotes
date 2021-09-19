在项目中,下载普通图片(png,jpg等)都正常,但是svg图片不能下载,主要问题是返回的请求头不对,要写成如下:

```
"Content-Type", "image/svg+xml;charset=UTF-8"
```

完整代码:
```
	@RequestMapping("/downLoadImage")
	public BaseResponse downLoadImage(@Param("imageName") String imageName, HttpServletResponse resp) {
		logger.info("下载图片开始-入参：{}", imageName);
		BaseResponse response = new BaseResponse();
		FileImageInputStream fs = null;
		ServletOutputStream os = null;
		try {
			if (StringUtil.isNullOrEmpty(imageName)) {
				logger.info("图片名称为空");
				setResponse(response, ResponseCode.JIESHUN0003.getCode());
				return response;
			}
			File file = ImageUtil.downLoadImage(imageName, response);
			if (file == null) {
				logger.info("没有找到图片..");
				return response;
			}
			// 如果是svg格式后缀的头信息不一样
			String suffix = imageName.substring(imageName.lastIndexOf("."));
			fs = new FileImageInputStream(file);
			int len = (int) fs.length();
			byte[] bs = new byte[len];
			fs.read(bs, 0, len);
			if (".svg".equals(suffix)) {
				resp.setHeader("Content-Type", "image/svg+xml;charset=UTF-8");
				resp.setHeader("Accept-Ranges", "bytes");
			} else {
				resp.setHeader("Content-Type", "application/octet-stream");
				resp.setHeader("Content-Disposition", "attachment;filename=" + file.getName());
			}
			os = resp.getOutputStream();
			os.write(bs);
			os.flush();
		} catch (Exception e) {
			logger.error("下载图片失败，入参：{}", imageName, e);
			setSysErrorResponse(response);
			return response;
		} finally {
			logger.info("下载图片结束。 返参：{}", response);
			try {
				if (fs != null) {
					fs.close();
				}
			} catch (IOException e) {
				logger.error("流关闭异常！", e);
			}

			try {
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
				logger.error("流关闭异常！", e);
			}
		}
		return null;
	}

```