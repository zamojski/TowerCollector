<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Access token (API key) is special string used to authorize user to upload collected measurements to OpenCellID database.

The correct key must consist of digits 0-9 and letters a-f. Depending on registration date key format may look like this:

* `pk.9743a66f914cc249efca164485a19c5c` - 32 characters, preceded with `pk.`, total 35 characters
* `9743a66f914cc249efca164485a19c5c` - 32 characters
* `9743a66f-914c-c249-efca-164485a19c5c` - 32 characters in five groups separated by dash, total 36 characters
* `9743a66f914cc2` - 14 characters