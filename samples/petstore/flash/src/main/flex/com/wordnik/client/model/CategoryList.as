package com.wordnik.client.model {

import com.wordnik.swagger.common.ListWrapper;
public class CategoryList implements ListWrapper {
        // This declaration below of __obj_class is to force flash compiler to include this class
        private var _category_obj_class: com.wordnik.client.model.Category = null;
        [XmlElements(name="category", type="com.wordnik.client.model.Category")]
        public var category: Array = new Array();

        public function getList(): Array{
            return category;
        }

}
}

