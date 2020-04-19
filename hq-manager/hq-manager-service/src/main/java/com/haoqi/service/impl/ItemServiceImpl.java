package com.haoqi.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
//import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import com.haoqi.mapper.TbItemDescMapper;
import com.haoqi.mapper.TbItemMapper;
import com.haoqi.pojo.TbItem;
import com.haoqi.pojo.TbItemDesc;
import com.haoqi.pojo.TbItemExample;
import com.haoqi.service.ItemService;
import common.jedis.JedisClient;
import common.pojo.EasyUIDataGridResult;
import common.utils.E3Result;
import common.utils.IDUtils;
import common.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;



/**
 * 商品管理Service
 * <p>Title: ItemServiceImpl</p>
 * <p>Description: </p>
 * <p>Company: www.itcast.cn</p> 
 * @version 1.0
 */
@Service
public class ItemServiceImpl implements ItemService {

	@Autowired
	private TbItemMapper itemMapper;
	@Autowired
	private TbItemDescMapper itemDescMapper;
//	@Autowired
//	private JmsTemplate JmsTemplate;
//	@Resource
//	private Destination topicDestination;
	@Autowired
	private JedisClient jedisClient;
	@Value("${REDIS_ITEM_PRE}")
	private String REDIS_ITEM_PRE;
	@Value("${ITEM_CACHE_EXPIRE}")
	private Integer ITEM_CACHE_EXPIRE;
	@Override
	public TbItem getItemById(long itemId) {
		//查询缓存
		try {
			String json = jedisClient.get(REDIS_ITEM_PRE + ":" + itemId + ":BASE");
			if (StringUtils.isNotBlank(json)) {
				TbItem tbItem = JsonUtils.jsonToPojo(json, TbItem.class);
				return tbItem;
			}
		} catch (Exception e) {
		}
		//缓存中没有，查询数据库
		//根据主键查询
		//TbItem tbItem = itemMapper.selectByPrimaryKey(itemId);
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		//设置查询条件
		criteria.andIdEqualTo(itemId);
		//执行查询
		List<TbItem> list = itemMapper.selectByExample(example);
		if (list != null && list.size() > 0) {
			//把结果添加到缓存
			try {
				jedisClient.set(REDIS_ITEM_PRE + ":" + itemId + ":BASE", JsonUtils.objectToJson(list.get(0)));
				jedisClient.expire(REDIS_ITEM_PRE + ":" + itemId + ":BASE", ITEM_CACHE_EXPIRE);
			} catch (Exception e) {
				// TODO: handle exception
			}
			return list.get(0);
		}
		return null;
	}

	@Override
	public EasyUIDataGridResult getItemList(int page, int rows) {
		//设置分页信息
		PageHelper.startPage(page, rows);
		//执行查询
		TbItemExample example = new TbItemExample();
		List<TbItem> list = itemMapper.selectByExample(example);
		//创建一个返回值对象
		EasyUIDataGridResult result = new EasyUIDataGridResult();
		result.setRows(list);
		//取分页结果
		PageInfo<TbItem> pageInfo = new PageInfo<>(list);
		//取总记录数
		long total = pageInfo.getTotal();
		result.setTotal(total);
		return result;
	}
 
	@Override
	public E3Result addItem(TbItem item, String desc) {
		// 1、生成商品id
				final long itemId = IDUtils.genItemId();
				// 2、补全TbItem对象的属性
				item.setId(itemId);
				//商品状态，1-正常，2-下架，3-删除
				item.setStatus((byte) 1);
				Date date = new Date();
				item.setCreated(date);
				item.setUpdated(date);
				// 3、向商品表插入数据
				itemMapper.insert(item);
				// 4、创建一个TbItemDesc对象
				TbItemDesc itemDesc = new TbItemDesc();
				// 5、补全TbItemDesc的属性
				itemDesc.setItemId(itemId);
				itemDesc.setItemDesc(desc);
				itemDesc.setCreated(date);
				itemDesc.setUpdated(date);
				// 6、向商品描述表插入数据
				itemDescMapper.insert(itemDesc);
				//发送一个商品添加消息
//				JmsTemplate.send(topicDestination, new MessageCreator() {
//
//					@Override
//					public Message createMessage(Session session) throws JMSException {
//						TextMessage textMessage = session.createTextMessage(itemId + "");
//						return textMessage;
//					}
//				});
				// 7、E3Result.ok()
				return E3Result.ok();
	}

	@Override
	public TbItemDesc getItemDescById(long itemId) {
		//查询缓存
				try {
					String json = jedisClient.get(REDIS_ITEM_PRE + ":" + itemId + ":DESC");
					if (StringUtils.isNotBlank(json)) {
						TbItemDesc tbItemDesc = JsonUtils.jsonToPojo(json, TbItemDesc.class);
						return tbItemDesc;
					}
				} catch (Exception e) {
				}
				
		TbItemDesc itemDesc = itemDescMapper.selectByPrimaryKey(itemId);
	 
			//把结果添加到缓存
			try {
				jedisClient.set(REDIS_ITEM_PRE + ":" + itemId + ":DESC", JsonUtils.objectToJson(itemDesc));
				jedisClient.expire(REDIS_ITEM_PRE + ":" + itemId + ":DESC", ITEM_CACHE_EXPIRE);
			} catch (Exception e) {
				// TODO: handle exception
			}
		 
		 
		return itemDesc;
	}

}
