package cn.ecomb.jackcat.utils.digester;

import cn.ecomb.jackcat.utils.digester.rule.*;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.*;

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

		// 入栈一个保存元素节点内容的 StringBuilder
		bodyTexts.push(new StringBuilder());
		StringBuilder sb = new StringBuilder(match);
		if (match.length() > 0) {
			sb.append('/');
		}
		sb.append(qName);
		match = sb.toString();
		log.debug("  New match='{}'", match);

		List<Rule> matchRules = matchRules(match);
		matches.push(matchRules);
		if (matchRules != null && matchRules.size() > 0) {
			for (int i = 0; i < matchRules.size(); i++) {
				try {
					Rule rule = matchRules.get(i);
					log.debug("  Fire begin() for {}", rule);
					rule.begin(uri, qName, attributes);
				} catch (Exception e) {
					log.error("Begin event threw exception", e);
					throw new SAXException(e);
				}
			}
		} else {
			log.debug("  No rules found matching '{}'.", match);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		log.debug("characters(...)");

		// 获取当前元素节点关联的 StringBuilder，添加内容
		StringBuilder bodyText = bodyTexts.peek();
		bodyText.append(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		List<Rule> matchRules = matches.pop();
		StringBuilder bodyText = bodyTexts.pop();
		if (log.isDebugEnabled()) {
			log.debug("endElement({},{},{})", uri, localName, qName);
			log.debug("  match='{}'", match);
			log.debug("  bodyText='{}'", bodyText.toString().trim());
		}
		if (matchRules != null && matchRules.size() > 0) {
			for (int i = 0; i < matchRules.size(); i++) {
				try {
					Rule rule = matchRules.get(i);
					log.debug("  Fire body() for {}", rule);
					rule.body(uri, qName, bodyText.toString());
				} catch (Exception e) {
					log.error("Body event threw exception", e);
					throw new SAXException(e);
				}
			}
		} else {
			log.debug("  No rules found matching '{}'.", match);
		}

		for (int i = 0; i < matchRules.size(); i++) { // 倒叙遍历
			int j = (matchRules.size() - 1) - i;
			try {
				Rule rule = matchRules.get(j);
				log.debug("  Fire end() for " + rule);
				rule.end(uri, qName);
			} catch (Exception e) {
				log.error("End event threw exception", e);
				throw new SAXException(e);
			}
		}
		// 恢复上一个匹配的表达式
		int slash = match.lastIndexOf('/');
		if (slash >= 0) {
			match = match.substring(0, slash);
		} else {
			match = "";
		}
	}

	@Override
	public void endDocument() throws SAXException {
		match = "";
		stack.clear();
	}
	// End Sax method

	public String match() {
		return match;
	}

	// 对象栈的操作
	public void push(Object obj) {
		if (stack.size() == 0) {
			root = obj;
		}
		stack.push(obj);
	}

	public Object pop() {
		return stack.pop();
	}

	public Object peek() {
		return stack.peek();
	}

	public Object peek(int index) {
		return stack.get(index);
	}

	public void addSetFields(String pattern) {
		SetFieldsRule setFieldsRule = new SetFieldsRule();
		setFieldsRule.setDigester(this);
		addRule(pattern, setFieldsRule);
	}

	public void addObjectCreate(String pattern, String clazz) {
		ObjectCreateRule objectCreateRule = new ObjectCreateRule(clazz);
		objectCreateRule.setDigester(this);
		addRule(pattern, objectCreateRule);
	}

	public void addSetNext(String pattern, String methodName) {
		SetNextRule setNextRule = new SetNextRule(methodName);
		setNextRule.setDigester(this);
		addRule(pattern, setNextRule);
	}

	public void addSetNext(String pattern, String methodName, String paramType) {
		SetNextRule setNextRule = new SetNextRule(methodName, paramType);
		setNextRule.setDigester(this);
		addRule(pattern, setNextRule);
	}

	public void addCallMethod(String pattern, String methodName, int paramCount) {
		addCallMethod(pattern, methodName, paramCount, null);
	}

	public void addCallMethod(String pattern, String methodName, int paramCount, Class<?>[] paramsType) {
		CallMethodRule callMethod = new CallMethodRule(methodName, paramCount, paramsType);
		callMethod.setDigester(this);
		addRule(pattern, callMethod);
	}

	public void addCallParam(String pattern, int paramIndex) {
		addCallParam(pattern, paramIndex, null);
	}

	public void addCallParam(String pattern, int paramIndex, String attributeName) {
		CallParamRule callParam = new CallParamRule(paramIndex, attributeName);
		callParam.setDigester(this);
		addRule(pattern, callParam);
	}

	public void addCallMethodMultiRule(String pattern, String methodName, int paramCount, int multiParamIndex) {
		CallMethodMultiRule callMethodMulti = new CallMethodMultiRule(methodName, paramCount, multiParamIndex);
		callMethodMulti.setDigester(this);
		addRule(pattern, callMethodMulti);
	}

	public void addCallParamMultiRule(String pattern, int paramIndex) {
		CallParamMultiRule callParamMulti = new CallParamMultiRule(paramIndex);
		callParamMulti.setDigester(this);
		addRule(pattern, callParamMulti);
	}

	// rules
	public void addRule(String pattern, Rule rule) {
		// to help users who accidently add '/' to the end of their patterns
		int patternLength = pattern.length();
		if (patternLength > 1 && pattern.endsWith("/")) {
			pattern = pattern.substring(0, patternLength - 1);
		}

		List<Rule> list = rules.get(pattern);
		if (list == null) {
			list = new ArrayList<Rule>();
			rules.put(pattern, list);
		}
		list.add(rule);
	}

	public List<Rule> matchRules(String pattern) {
		List<Rule> rulesList = rules.get(pattern);

		if ((rulesList == null) || (rulesList.size() < 1)) {
			// Find the longest key, ie more discriminant
			String longKey = "";
			Iterator<String> keys = rules.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				if (key.startsWith("*/")) {
					if (pattern.equals(key.substring(2)) || pattern.endsWith(key.substring(1))) {
						if (key.length() > longKey.length()) {
							rulesList = rules.get(key);
							longKey = key;
						}
					}
				}
			}
		}
		if (rulesList == null) {
			rulesList = new ArrayList<Rule>();
		}
		return (rulesList);
	}

	public void setClassLoader(ClassLoader cl) {
		classLoader = cl;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}
}
