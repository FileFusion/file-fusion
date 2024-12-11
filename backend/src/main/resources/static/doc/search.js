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
    url: '/api/user/current/org',
    methodId: '1eb503d58e7bb7c2ce0e5360e3db416e',
    desc: 'get current user org',
});
api[0].list[1].list.push({
    order: '4',
    deprecated: 'false',
    url: '/api/user/current',
    methodId: '97eb5961ede8cbf36a93139fb151a9bc',
    desc: 'update current user',
});
api[0].list[1].list.push({
    order: '5',
    deprecated: 'false',
    url: '/api/user/password/current',
    methodId: 'b1adcf0e0e2928b53627cbad11f7fd91',
    desc: 'update current user password',
});
api[0].list[1].list.push({
    order: '6',
    deprecated: 'false',
    url: '/api/user/{page}/{pageSize}',
    methodId: '4ce602bbc01d2825e7fb9d462a42e707',
    desc: 'get user list - paged',
});
api[0].list[1].list.push({
    order: '7',
    deprecated: 'false',
    url: '/api/user',
    methodId: '42869d68afa6b16bcf912f0d6da4e114',
    desc: 'add user',
});
api[0].list[1].list.push({
    order: '8',
    deprecated: 'false',
    url: '/api/user',
    methodId: 'f4c5f0bb33875c23907a743cfb734e59',
    desc: 'update user',
});
api[0].list[1].list.push({
    order: '9',
    deprecated: 'false',
    url: '/api/user/_batch_delete',
    methodId: '3f46571a369247f94942c1bdc958d196',
    desc: 'batch delete user',
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
    methodId: 'd765a8b5b1c30e9498300ffbdead9ed1',
    desc: 'update org',
});
api[0].list[2].list.push({
    order: '3',
    deprecated: 'false',
    url: '/api/org',
    methodId: 'c967c374282cda1b4a9dd10aa896c8e4',
    desc: 'add org',
});
api[0].list[2].list.push({
    order: '4',
    deprecated: 'false',
    url: '/api/org/{orgId}/users/{page}/{pageSize}',
    methodId: '1f893a91df3a16a087d9f0926dd76752',
    desc: 'get org user list - paged',
});
api[0].list[2].list.push({
    order: '5',
    deprecated: 'false',
    url: '/api/org/{orgId}/users/not_exits',
    methodId: 'e89739583fc082bb6eb9120cee4d6198',
    desc: 'get not exits org user',
});
api[0].list[2].list.push({
    order: '6',
    deprecated: 'false',
    url: '/api/org/{orgId}',
    methodId: '615aeff0d7e2da3be35e1ecac9fc58a0',
    desc: 'delete org',
});
api[0].list[2].list.push({
    order: '7',
    deprecated: 'false',
    url: '/api/org/{orgId}/_add_users',
    methodId: 'adc30f2edfd932fcda8c0907f22f8bbd',
    desc: 'add org user',
});
api[0].list[2].list.push({
    order: '8',
    deprecated: 'false',
    url: '/api/org/{orgId}/_remove_users',
    methodId: '22184f8c5ff7daf9acdb6e9dd294f8b9',
    desc: 'delete org user',
});
api[0].list.push({
    alias: 'RoleController',
    order: '4',
    link: 'rolecontroller',
    desc: 'RoleController',
    list: []
})
api[0].list[3].list.push({
    order: '1',
    deprecated: 'false',
    url: '/api/role',
    methodId: '71c60f34f1e52e1785d46a51e85dbfec',
    desc: 'get role list',
});
api[0].list[3].list.push({
    order: '2',
    deprecated: 'false',
    url: '/api/role',
    methodId: '21ef1892558ee9d6840153b19a5c4514',
    desc: 'update role',
});
api[0].list[3].list.push({
    order: '3',
    deprecated: 'false',
    url: '/api/role',
    methodId: '26778cf8e07e9fd73ad683c78cd2a7f1',
    desc: 'add role',
});
api[0].list[3].list.push({
    order: '4',
    deprecated: 'false',
    url: '/api/role/{roleId}',
    methodId: '056fe7d9a8f5221970e3b6dfc03f2b0c',
    desc: 'delete role',
});
api[0].list[3].list.push({
    order: '5',
    deprecated: 'false',
    url: '/api/role/{roleId}/permission',
    methodId: '05b142d1dca29c27545f3f4bad772cf2',
    desc: 'get role permission list',
});
api[0].list.push({
    alias: 'PermissionController',
    order: '5',
    link: 'permissioncontroller',
    desc: 'PermissionController',
    list: []
})
api[0].list[4].list.push({
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