package com.haoqi.service;
import common.pojo.EasyUITreeNode;

import java.util.List;

public interface ItemCatService {

	List<EasyUITreeNode> getItemCatlist(long parentId);
}
