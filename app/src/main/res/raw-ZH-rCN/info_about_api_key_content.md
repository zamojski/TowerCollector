<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
访问令牌（API密钥）是用于授权用户将收集的测量数据上传到OpenCellID数据库的特殊字符串。

正确的密钥必须由数字0-9和字母a-f组成。根据注册日期，密钥格式可能如下所示：

* `pk.9743a66f914cc249efca164485a19c5c` - 32个字符，前缀`pk.`，共35个字符
* `9743a66f914cc249efca164485a19c5c` - 共32个字符
* `9743a66f-914c-c249-efca-164485a19c5c` - 由破折号分隔的五个组中的32个字符， 共36个字符
* `9743a66f914cc2` - 共14个字符