package com.haoqi.service;

import com.haoqi.pojo.TbItem;
import com.haoqi.pojo.TbItemDesc;
import common.pojo.EasyUIDataGridResult;
import common.utils.E3Result;

public interface ItemService {

	TbItem getItemById(long itemId);
	EasyUIDataGridResult getItemList(int page, int rows);
	E3Result addItem(TbItem item, String desc);
	TbItemDesc getItemDescById(long itemId);
}
