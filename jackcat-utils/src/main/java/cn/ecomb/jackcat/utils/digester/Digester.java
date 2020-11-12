package cn.ecomb.jackcat.utils.digester;

import lombok.extern.slf4j.Slf4j;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import sun.plugin.javascript.navig.Link;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 使用 sex 解析 web.xml 配置文件
 *
 * 这里可以思考一下，是不是可以用配置文件的形式，使用 SPI 机制
 *
 * @author brian.zhou
 * @date 2020/11/9
 */
@Slf4j
public class Digester extends DefaultHandler {

	private Object root;

	/** 默认使用当前线程的类加载器 */
	protected ClassLoader classLoader;

	/** 对象栈 */
	private LinkedList<Object> stack = new LinkedList<>();

	/** 解析过程中，节点对应的匹配规则 */
	private LinkedList<List<Rule>> matches = new LinkedList<>();

	/** 当前节点包含的元素 */
	private LinkedList<StringBuilder> bodyTexts = new LinkedList<>();

	/** 匹配规则 */
	private HashMap<String, List<Rule>> rules = new HashMap<>();

	/** 当前匹配的 patten */
	private String match = "";

	public Object parse(InputSource in) throws IOException, SAXException {
		XMLReader xmlReader = null;
		try {
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			xmlReader = saxParser.getXMLReader();
		} catch (ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		}
		xmlReader.setContentHandler(this);
		xmlReader.parse(in);
		return root;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		log.debug("startElement, uri:{}, localName:{}, qName:{}", uri, localName, qName);


	}
}
