>���̳̽��򵥽���һ�������AOSuite�¿���Http�ӿڣ���ǰ��Web��App������ϵͳ/ģ����á� ���ݽӿڽ�����ʽ�ܶ࣬�Ͳ�չ�������ˡ�����ȱʡʹ��httpЭ��+json���л���ʽ�������������Ϊ�ӿڽ�����ʽ����ʾ������ʵ������ķ������� aos.showcase.api.CreditCardController.java��aos.test.testcase.HttpClientTest.java

#### ��1. �����ӿڼ�����ʵ��һ
���ַ�ʽʹ�ñ��ύ�ķ�ʽ���������������Content TypeΪapplication/x-www-form-urlencoded��  
**�����ӿ�** 
```
    /**
	 * ע��ӿ�
	 * 
	 * ���ַ�ʽ��ǰ����H5 Ajax��dataType:"json",contentType:"application/json"ʱ��Android��
	 * IOSͨ��Http���ֱ���ύJSON�����
	 * 
	 * @param jsonString
	 * @param response
	 */
	@RequestMapping(value = "register2")
	public void register2(@RequestBody String jsonString, HttpServletRequest request, HttpServletResponse response) {
		System.out.println(jsonString);
		// ����ͨ�������API��JSON�����л���Dto
		Dto inDto = AOSJson.fromJson(jsonString, HashDto.class);
		inDto.println();
		WebCxt.write(response, "�յ�����!");
	}
```
**���ýӿ�** 
```
    /**
	 * POST���� ��JSON������ʽ�ύ����
	 * (�ӿ�ʵ�ֺͽӿڵ��������׵ģ�����ӿ�ʵ�֣�api/creditCard/register2)
	 */
	public static void doPost2() {
		HttpRequestVO httpRequestVO = new HttpRequestVO("http://localhost:10010/AOSuite/api/creditCard/register2");
		httpRequestVO.setJsonEntityData("{"mobile_":"18616786188","name_":"�ܴ�"}");
		HttpResponseVO httpResponseVO = AOSHttpClient.execute(httpRequestVO);
		System.out.println("HTTP״̬�룺" + httpResponseVO.getStatus());
		System.out.println("����ֵ��" + httpResponseVO.getOut());
	}
```

#### ��2. �����ӿڼ�����ʵ����
���ַ�ʽʹ�ñ��ύ�ķ�ʽ���������������Content TypeΪapplication/x-www-form-urlencoded��  
**�����ӿ�** 
```
    /**
	 * ע��ӿ�
	 * 
	 * ���ַ�ʽ��ǰ����H5 Ajax��dataType:"json",contentType:"application/json"ʱ��Android��
	 * IOSͨ��Http���ֱ���ύJSON�����
	 * 
	 * @param jsonString
	 * @param response
	 */
	@RequestMapping(value = "register2")
	public void register2(@RequestBody String jsonString, HttpServletRequest request, HttpServletResponse response) {
		System.out.println(jsonString);
		// ����ͨ�������API��JSON�����л���Dto
		Dto inDto = AOSJson.fromJson(jsonString, HashDto.class);
		inDto.println();
		WebCxt.write(response, "�յ�����!");
	}

```
**���ýӿ�** 
```
    /**
	 * POST���� ��JSON������ʽ�ύ����
	 * (�ӿ�ʵ�ֺͽӿڵ��������׵ģ�����ӿ�ʵ�֣�api/creditCard/register2)
	 */
	public static void doPost2() {
		HttpRequestVO httpRequestVO = new HttpRequestVO("http://localhost:10010/AOSuite/api/creditCard/register2");
		httpRequestVO.setJsonEntityData("{"mobile_":"18616786188","name_":"�ܴ�"}");
		HttpResponseVO httpResponseVO = AOSHttpClient.execute(httpRequestVO);
		System.out.println("HTTP״̬�룺" + httpResponseVO.getStatus());
		System.out.println("����ֵ��" + httpResponseVO.getOut());
	} 
```

#### ��3. �����ӿڼ�����ʵ����
���ַ�ʽʹ�ñ��ύ�ķ�ʽ���������������Content TypeΪapplication/x-www-form-urlencoded��  
**�����ӿ�**
```
    /**
	 * resetful����Http�ӿ�
	 * 
	 * @param version
	 * @param id
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "info/{id}", method = RequestMethod.GET)
	public void getCardInfo(@PathVariable String version, @PathVariable String id, HttpServletRequest request,
			HttpServletResponse response) {
		System.out.println(id);
		WebCxt.write(response, "�յ�����!");
	}
```

**���ýӿ�**
```
    /**
	 * GET����
	 */
	public static void doGet() {
		Map inMap = Maps.newHashMap();
		inMap.put("name", "�ܴ�");
		inMap.put("age", "30");
		HttpRequestVO httpRequestVO = new HttpRequestVO("http://localhost:10010/AOSuite/api/creditCard/info/1000", inMap);
		httpRequestVO.setRequestMethod(AOSHttpClient.REQUEST_METHOD.GET);
		HttpResponseVO httpResponseVO = AOSHttpClient.execute(httpRequestVO);
		System.out.println("HTTP״̬�룺" + httpResponseVO.getStatus());
		System.out.println("����ֵ��" + httpResponseVO.getOut());
	}
```