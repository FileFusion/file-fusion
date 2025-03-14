let api = [];
const apiDocListSize = 1
api.push({
    name: 'default',
    order: '1',
    list: []
})
api[0].list.push({
    alias: 'LicenseController',
    order: '1',
    link: 'licensecontroller',
    desc: 'LicenseController',
    list: []
})
api[0].list[0].list.push({
    order: '1',
    deprecated: 'false',
    url: '/api/license/current',
    methodId: 'd9dca096b0af12e90395f31f32127871',
    desc: 'get current license',
});
api[0].list.push({
    alias: 'UserController',
    order: '2',
    link: 'usercontroller',
    desc: 'UserController',
    list: []
})
api[0].list[1].list.push({
    order: '1',
    deprecated: 'false',
    url: '/api/user/_login',
    methodId: 'df371d8afa0772afa2c5c3af539c505f',
    desc: 'user login',
});
api[0].list[1].list.push({
    order: '2',
    deprecated: 'false',
    url: '/api/user/current',
    methodId: '8685bf45d9747c25d033e501ce981361',
    desc: 'get current user',
});
api[0].list[1].list.push({
    order: '3',
    deprecated: 'false',
    url: '/api/user/current',
    methodId: '8f9239804620badca866902c1d744829',
    desc: 'update current user',
});
api[0].list[1].list.push({
    order: '4',
    deprecated: 'false',
    url: '/api/user/current/password',
    methodId: 'f600ca073f19b98e37ff9c37a7009cbc',
    desc: 'update current user password',
});
api[0].list[1].list.push({
    order: '5',
    deprecated: 'false',
    url: '/api/user/{page}/{pageSize}',
    methodId: 'ff8ce93d5f6a497316a38cd340188380',
    desc: 'get user list - paged',
});
api[0].list[1].list.push({
    order: '6',
    deprecated: 'false',
    url: '/api/user',
    methodId: '2498f40adc7f1c162e4b55c1bfea289b',
    desc: 'add user',
});
api[0].list[1].list.push({
    order: '7',
    deprecated: 'false',
    url: '/api/user',
    methodId: 'aba3d05fb4a5572e927e494d7b81a3d0',
    desc: 'update user',
});
api[0].list[1].list.push({
    order: '8',
    deprecated: 'false',
    url: '/api/user/{userId}',
    methodId: '5bfa0e729eaae180920b44a2b0f53a83',
    desc: 'delete user',
});
api[0].list.push({
    alias: 'OrgController',
    order: '3',
    link: 'orgcontroller',
    desc: 'OrgController',
    list: []
})
api[0].list[2].list.push({
    order: '1',
    deprecated: 'false',
    url: '/api/org',
    methodId: 'c06afe6d74732c2aa5b6f26300d73e16',
    desc: 'get org list',
});
api[0].list[2].list.push({
    order: '2',
    deprecated: 'false',
    url: '/api/org',
    methodId: '90d81556ef973529d7f165412b07f5ee',
    desc: 'add org',
});
api[0].list[2].list.push({
    order: '3',
    deprecated: 'false',
    url: '/api/org',
    methodId: 'afce49f5f2530e8b1b8e36bbab0a825a',
    desc: 'update org',
});
api[0].list[2].list.push({
    order: '4',
    deprecated: 'false',
    url: '/api/org/{orgId}',
    methodId: '57130408f70f9b2d6d68420956656bce',
    desc: 'delete org',
});
api[0].list[2].list.push({
    order: '5',
    deprecated: 'false',
    url: '/api/org/{orgId}/user/{page}/{pageSize}',
    methodId: '3a6f06099f08679b328878ea04c9d065',
    desc: 'get org user list - paged',
});
api[0].list[2].list.push({
    order: '6',
    deprecated: 'false',
    url: '/api/org/{orgId}/user/not_exits',
    methodId: 'e80592a574155393b08a45ea066f054e',
    desc: 'get not exits org user',
});
api[0].list[2].list.push({
    order: '7',
    deprecated: 'false',
    url: '/api/org/{orgId}/user',
    methodId: 'adc30f2edfd932fcda8c0907f22f8bbd',
    desc: 'add org user',
});
api[0].list[2].list.push({
    order: '8',
    deprecated: 'false',
    url: '/api/org/{orgId}/user/{userId}',
    methodId: '22184f8c5ff7daf9acdb6e9dd294f8b9',
    desc: 'delete org user',
});
api[0].list.push({
    alias: 'FileDataController',
    order: '4',
    link: 'filedatacontroller',
    desc: 'FileDataController',
    list: []
})
api[0].list[3].list.push({
    order: '1',
    deprecated: 'false',
    url: '/api/file_data/{page}/{pageSize}',
    methodId: 'f619a3ce3e2f4dc727189a664e935138',
    desc: 'get file list - paged',
});
api[0].list[3].list.push({
    order: '2',
    deprecated: 'false',
    url: '/api/file_data/{id}',
    methodId: '843b87f25883c0ec7f22bf5b85db57be',
    desc: 'delete file',
});
api[0].list[3].list.push({
    order: '3',
    deprecated: 'false',
    url: '/api/file_data/folder',
    methodId: '3b9d2aaed797730efc3f3a6d7c9464d6',
    desc: 'create folder',
});
api[0].list[3].list.push({
    order: '4',
    deprecated: 'false',
    url: '/api/file_data/_upload_chunk',
    methodId: 'e5249a6dc3462b55661be90b207b0a63',
    desc: 'upload file chunk',
});
api[0].list[3].list.push({
    order: '5',
    deprecated: 'false',
    url: '/api/file_data/_upload_chunk_merge',
    methodId: 'bf3aa6f48e2bf4105ddedaf5f0802893',
    desc: 'upload chunk merge',
});
api[0].list[3].list.push({
    order: '6',
    deprecated: 'false',
    url: '/api/file_data/_rename',
    methodId: '99c3ad2099c6fe6b6acc3ccfa91a22ca',
    desc: 'rename file',
});
api[0].list[3].list.push({
    order: '7',
    deprecated: 'false',
    url: '/api/file_data/_submit_download',
    methodId: '5f8bfc42404b9dae602fcb80b8fc63ea',
    desc: 'submit download file list',
});
api[0].list[3].list.push({
    order: '8',
    deprecated: 'false',
    url: '/api/file_data/_download/{downloadId}',
    methodId: '776799877f01434157432acaa1e0256d',
    desc: 'download file list',
});
api[0].list[3].list.push({
    order: '9',
    deprecated: 'false',
    url: '/api/file_data/_download_chunked/{id}',
    methodId: 'b500c72e332077f6114a75fa37294140',
    desc: 'download chunked',
});
api[0].list[3].list.push({
    order: '10',
    deprecated: 'false',
    url: '/api/file_data/thumbnail/{id}',
    methodId: 'b475488fe13382f22b1dea1a5b5fa8b0',
    desc: 'thumbnail file',
});
api[0].list.push({
    alias: 'SysConfigController',
    order: '5',
    link: 'sysconfigcontroller',
    desc: 'SysConfigController',
    list: []
})
api[0].list[4].list.push({
    order: '1',
    deprecated: 'false',
    url: '/api/sys-config',
    methodId: '695a0942598f329a2e0ec5d42a3610eb',
    desc: 'get sys config',
});
api[0].list[4].list.push({
    order: '2',
    deprecated: 'false',
    url: '/api/sys-config',
    methodId: '14e25b01ceb601e345d2b832a2c8b00f',
    desc: 'update sys config',
});
api[0].list.push({
    alias: 'RoleController',
    order: '6',
    link: 'rolecontroller',
    desc: 'RoleController',
    list: []
})
api[0].list[5].list.push({
    order: '1',
    deprecated: 'false',
    url: '/api/role',
    methodId: '71c60f34f1e52e1785d46a51e85dbfec',
    desc: 'get role list',
});
api[0].list[5].list.push({
    order: '2',
    deprecated: 'false',
    url: '/api/role',
    methodId: 'a8deb55585dd4dad4e0c7cbd1ae758f2',
    desc: 'add role',
});
api[0].list[5].list.push({
    order: '3',
    deprecated: 'false',
    url: '/api/role',
    methodId: '0875307d23ad4ed44f0ef2cc9c293fb6',
    desc: 'update role',
});
api[0].list[5].list.push({
    order: '4',
    deprecated: 'false',
    url: '/api/role/{roleId}',
    methodId: '056fe7d9a8f5221970e3b6dfc03f2b0c',
    desc: 'delete role',
});
api[0].list[5].list.push({
    order: '5',
    deprecated: 'false',
    url: '/api/role/{roleId}/permission',
    methodId: '05b142d1dca29c27545f3f4bad772cf2',
    desc: 'get role permission list',
});
api[0].list.push({
    alias: 'PermissionController',
    order: '7',
    link: 'permissioncontroller',
    desc: 'PermissionController',
    list: []
})
api[0].list[6].list.push({
    order: '1',
    deprecated: 'false',
    url: '/api/permission',
    methodId: '9b1b6b8352b4baa0cd5284cafdbc571d',
    desc: 'get permission list',
});
document.onkeydown = keyDownSearch;
function keyDownSearch(e) {
    const theEvent = e;
    const code = theEvent.keyCode || theEvent.which || theEvent.charCode;
    if (code === 13) {
        const search = document.getElementById('search');
        const searchValue = search.value.toLocaleLowerCase();

        let searchGroup = [];
        for (let i = 0; i < api.length; i++) {

            let apiGroup = api[i];

            let searchArr = [];
            for (let i = 0; i < apiGroup.list.length; i++) {
                let apiData = apiGroup.list[i];
                const desc = apiData.desc;
                if (desc.toLocaleLowerCase().indexOf(searchValue) > -1) {
                    searchArr.push({
                        order: apiData.order,
                        desc: apiData.desc,
                        link: apiData.link,
                        alias: apiData.alias,
                        list: apiData.list
                    });
                } else {
                    let methodList = apiData.list || [];
                    let methodListTemp = [];
                    for (let j = 0; j < methodList.length; j++) {
                        const methodData = methodList[j];
                        const methodDesc = methodData.desc;
                        if (methodDesc.toLocaleLowerCase().indexOf(searchValue) > -1) {
                            methodListTemp.push(methodData);
                            break;
                        }
                    }
                    if (methodListTemp.length > 0) {
                        const data = {
                            order: apiData.order,
                            desc: apiData.desc,
                            link: apiData.link,
                            alias: apiData.alias,
                            list: methodListTemp
                        };
                        searchArr.push(data);
                    }
                }
            }
            if (apiGroup.name.toLocaleLowerCase().indexOf(searchValue) > -1) {
                searchGroup.push({
                    name: apiGroup.name,
                    order: apiGroup.order,
                    list: searchArr
                });
                continue;
            }
            if (searchArr.length === 0) {
                continue;
            }
            searchGroup.push({
                name: apiGroup.name,
                order: apiGroup.order,
                list: searchArr
            });
        }
        let html;
        if (searchValue === '') {
            const liClass = "";
            const display = "display: none";
            html = buildAccordion(api,liClass,display);
            document.getElementById('accordion').innerHTML = html;
        } else {
            const liClass = "open";
            const display = "display: block";
            html = buildAccordion(searchGroup,liClass,display);
            document.getElementById('accordion').innerHTML = html;
        }
        const Accordion = function (el, multiple) {
            this.el = el || {};
            this.multiple = multiple || false;
            const links = this.el.find('.dd');
            links.on('click', {el: this.el, multiple: this.multiple}, this.dropdown);
        };
        Accordion.prototype.dropdown = function (e) {
            const $el = e.data.el;
            let $this = $(this), $next = $this.next();
            $next.slideToggle();
            $this.parent().toggleClass('open');
            if (!e.data.multiple) {
                $el.find('.submenu').not($next).slideUp("20").parent().removeClass('open');
            }
        };
        new Accordion($('#accordion'), false);
    }
}

function buildAccordion(apiGroups, liClass, display) {
    let html = "";
    if (apiGroups.length > 0) {
        if (apiDocListSize === 1) {
            let apiData = apiGroups[0].list;
            let order = apiGroups[0].order;
            for (let j = 0; j < apiData.length; j++) {
                html += '<li class="'+liClass+'">';
                html += '<a class="dd" href="#' + apiData[j].alias + '">' + apiData[j].order + '.&nbsp;' + apiData[j].desc + '</a>';
                html += '<ul class="sectlevel2" style="'+display+'">';
                let doc = apiData[j].list;
                for (let m = 0; m < doc.length; m++) {
                    let spanString;
                    if (doc[m].deprecated === 'true') {
                        spanString='<span class="line-through">';
                    } else {
                        spanString='<span>';
                    }
                    html += '<li><a href="#' + doc[m].methodId + '">' + apiData[j].order + '.' + doc[m].order + '.&nbsp;' + spanString + doc[m].desc + '<span></a> </li>';
                }
                html += '</ul>';
                html += '</li>';
            }
        } else {
            for (let i = 0; i < apiGroups.length; i++) {
                let apiGroup = apiGroups[i];
                html += '<li class="'+liClass+'">';
                html += '<a class="dd" href="#_'+apiGroup.order+'_' + apiGroup.name + '">' + apiGroup.order + '.&nbsp;' + apiGroup.name + '</a>';
                html += '<ul class="sectlevel1">';

                let apiData = apiGroup.list;
                for (let j = 0; j < apiData.length; j++) {
                    html += '<li class="'+liClass+'">';
                    html += '<a class="dd" href="#' + apiData[j].alias + '">' +apiGroup.order+'.'+ apiData[j].order + '.&nbsp;' + apiData[j].desc + '</a>';
                    html += '<ul class="sectlevel2" style="'+display+'">';
                    let doc = apiData[j].list;
                    for (let m = 0; m < doc.length; m++) {
                       let spanString;
                       if (doc[m].deprecated === 'true') {
                           spanString='<span class="line-through">';
                       } else {
                           spanString='<span>';
                       }
                       html += '<li><a href="#' + doc[m].methodId + '">'+apiGroup.order+'.' + apiData[j].order + '.' + doc[m].order + '.&nbsp;' + spanString + doc[m].desc + '<span></a> </li>';
                   }
                    html += '</ul>';
                    html += '</li>';
                }

                html += '</ul>';
                html += '</li>';
            }
        }
    }
    return html;
}