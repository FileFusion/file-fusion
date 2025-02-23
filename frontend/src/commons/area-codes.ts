import type { SUPPORT_LANGUAGES } from '@/commons/i18n.ts';

const areaCodes = [
  {
    'en-US': 'Angola',
    'zh-CN': '安哥拉',
    value: '+244'
  },
  {
    'en-US': 'Afghanistan',
    'zh-CN': '阿富汗',
    value: '+93'
  },
  {
    'en-US': 'Albania',
    'zh-CN': '阿尔巴尼亚',
    value: '+335'
  },
  {
    'en-US': 'Algeria',
    'zh-CN': '阿尔及利亚',
    value: '+213'
  },
  {
    'en-US': 'Andorra',
    'zh-CN': '安道尔共和国',
    value: '+376'
  },
  {
    'en-US': 'Anguilla',
    'zh-CN': '安圭拉岛',
    value: '+1254'
  },
  {
    'en-US': 'Antigua and Barbuda',
    'zh-CN': '安提瓜和巴布达',
    value: '+1268'
  },
  {
    'en-US': 'Argentina',
    'zh-CN': '阿根廷',
    value: '+54'
  },
  {
    'en-US': 'Armenia',
    'zh-CN': '亚美尼亚',
    value: '+374'
  },
  {
    'en-US': 'Ascension',
    'zh-CN': '阿森松',
    value: '+247'
  },
  {
    'en-US': 'Australia',
    'zh-CN': '澳大利亚',
    value: '+61'
  },
  {
    'en-US': 'Austria',
    'zh-CN': '奥地利',
    value: '+43'
  },
  {
    'en-US': 'Azerbaijan',
    'zh-CN': '阿塞拜疆',
    value: '+994'
  },
  {
    'en-US': 'Bahamas',
    'zh-CN': '巴哈马',
    value: '+1242'
  },
  {
    'en-US': 'Bahrain',
    'zh-CN': '巴林',
    value: '+973'
  },
  {
    'en-US': 'Bangladesh',
    'zh-CN': '孟加拉国',
    value: '+880'
  },
  {
    'en-US': 'Barbados',
    'zh-CN': '巴巴多斯',
    value: '+1246'
  },
  {
    'en-US': 'Belarus',
    'zh-CN': '白俄罗斯',
    value: '+375'
  },
  {
    'en-US': 'Belgium',
    'zh-CN': '比利时',
    value: '+32'
  },
  {
    'en-US': 'Belize',
    'zh-CN': '伯利兹',
    value: '+501'
  },
  {
    'en-US': 'Benin',
    'zh-CN': '贝宁',
    value: '+229'
  },
  {
    'en-US': 'Bermuda Is',
    'zh-CN': '百慕大群岛',
    value: '+1441'
  },
  {
    'en-US': 'Bolivia',
    'zh-CN': '玻利维亚',
    value: '+591'
  },
  {
    'en-US': 'Botswana',
    'zh-CN': '博茨瓦纳',
    value: '+267'
  },
  {
    'en-US': 'Brazil',
    'zh-CN': '巴西',
    value: '+55'
  },
  {
    'en-US': 'Brunei',
    'zh-CN': '文莱',
    value: '+673'
  },
  {
    'en-US': 'Bulgaria',
    'zh-CN': '保加利亚',
    value: '+359'
  },
  {
    'en-US': 'Burkina Faso',
    'zh-CN': '布基纳法索',
    value: '+226'
  },
  {
    'en-US': 'Burma',
    'zh-CN': '缅甸',
    value: '+95'
  },
  {
    'en-US': 'Burundi',
    'zh-CN': '布隆迪',
    value: '+257'
  },
  {
    'en-US': 'Cameroon',
    'zh-CN': '喀麦隆',
    value: '+237'
  },
  {
    'en-US': 'Canada',
    'zh-CN': '加拿大',
    value: '+1'
  },
  {
    'en-US': 'Cayman Is',
    'zh-CN': '开曼群岛',
    value: '+1345'
  },
  {
    'en-US': 'Central African Republic',
    'zh-CN': '中非共和国',
    value: '+236'
  },
  {
    'en-US': 'Chad',
    'zh-CN': '乍得',
    value: '+235'
  },
  {
    'en-US': 'Chile',
    'zh-CN': '智利',
    value: '+56'
  },
  {
    'en-US': 'China',
    'zh-CN': '中国',
    value: '+86'
  },
  {
    'en-US': 'Colombia',
    'zh-CN': '哥伦比亚',
    value: '+57'
  },
  {
    'en-US': 'Congo',
    'zh-CN': '刚果',
    value: '+242'
  },
  {
    'en-US': 'Cook Is',
    'zh-CN': '库克群岛',
    value: '+682'
  },
  {
    'en-US': 'Costa Rica',
    'zh-CN': '哥斯达黎加',
    value: '+506'
  },
  {
    'en-US': 'Cuba',
    'zh-CN': '古巴',
    value: '+53'
  },
  {
    'en-US': 'Cyprus',
    'zh-CN': '塞浦路斯',
    value: '+357'
  },
  {
    'en-US': 'Czech Republic',
    'zh-CN': '捷克',
    value: '+420'
  },
  {
    'en-US': 'Denmark',
    'zh-CN': '丹麦',
    value: '+45'
  },
  {
    'en-US': 'Djibouti',
    'zh-CN': '吉布提',
    value: '+253'
  },
  {
    'en-US': 'Dominica Rep',
    'zh-CN': '多米尼加共和国',
    value: '+1890'
  },
  {
    'en-US': 'Ecuador',
    'zh-CN': '厄瓜多尔',
    value: '+593'
  },
  {
    'en-US': 'Egypt',
    'zh-CN': '埃及',
    value: '+20'
  },
  {
    'en-US': 'EI Salvador',
    'zh-CN': '萨尔瓦多',
    value: '+503'
  },
  {
    'en-US': 'Estonia',
    'zh-CN': '爱沙尼亚',
    value: '+372'
  },
  {
    'en-US': 'Ethiopia',
    'zh-CN': '埃塞俄比亚',
    value: '+251'
  },
  {
    'en-US': 'Fiji',
    'zh-CN': '斐济',
    value: '+679'
  },
  {
    'en-US': 'Finland',
    'zh-CN': '芬兰',
    value: '+358'
  },
  {
    'en-US': 'France',
    'zh-CN': '法国',
    value: '+33'
  },
  {
    'en-US': 'French Guiana',
    'zh-CN': '法属圭亚那',
    value: '+594'
  },
  {
    'en-US': 'French Polynesia',
    'zh-CN': '法属玻利尼西亚',
    value: '+689'
  },
  {
    'en-US': 'Gabon',
    'zh-CN': '加蓬',
    value: '+241'
  },
  {
    'en-US': 'Gambia',
    'zh-CN': '冈比亚',
    value: '+220'
  },
  {
    'en-US': 'Georgia',
    'zh-CN': '格鲁吉亚',
    value: '+995'
  },
  {
    'en-US': 'Germany',
    'zh-CN': '德国',
    value: '+49'
  },
  {
    'en-US': 'Ghana',
    'zh-CN': '加纳',
    value: '+233'
  },
  {
    'en-US': 'Gibraltar',
    'zh-CN': '直布罗陀',
    value: '+350'
  },
  {
    'en-US': 'Greece',
    'zh-CN': '希腊',
    value: '+30'
  },
  {
    'en-US': 'Grenada',
    'zh-CN': '格林纳达',
    value: '+1809'
  },
  {
    'en-US': 'Guam',
    'zh-CN': '关岛',
    value: '+1671'
  },
  {
    'en-US': 'Guatemala',
    'zh-CN': '危地马拉',
    value: '+502'
  },
  {
    'en-US': 'Guinea',
    'zh-CN': '几内亚',
    value: '+224'
  },
  {
    'en-US': 'Guyana',
    'zh-CN': '圭亚那',
    value: '+592'
  },
  {
    'en-US': 'Haiti',
    'zh-CN': '海地',
    value: '+509'
  },
  {
    'en-US': 'Honduras',
    'zh-CN': '洪都拉斯',
    value: '+504'
  },
  {
    'en-US': 'Hongkong',
    'zh-CN': '香港',
    value: '+852'
  },
  {
    'en-US': 'Hungary',
    'zh-CN': '匈牙利',
    value: '+36'
  },
  {
    'en-US': 'Iceland',
    'zh-CN': '冰岛',
    value: '+354'
  },
  {
    'en-US': 'India',
    'zh-CN': '印度',
    value: '+91'
  },
  {
    'en-US': 'Indonesia',
    'zh-CN': '印度尼西亚',
    value: '+62'
  },
  {
    'en-US': 'Iran',
    'zh-CN': '伊朗',
    value: '+98'
  },
  {
    'en-US': 'Iraq',
    'zh-CN': '伊拉克',
    value: '+964'
  },
  {
    'en-US': 'Ireland',
    'zh-CN': '爱尔兰',
    value: '+353'
  },
  {
    'en-US': 'Israel',
    'zh-CN': '以色列',
    value: '+972'
  },
  {
    'en-US': 'Italy',
    'zh-CN': '意大利',
    value: '+39'
  },
  {
    'en-US': 'Ivory Coast',
    'zh-CN': '科特迪瓦',
    value: '+225'
  },
  {
    'en-US': 'Jamaica',
    'zh-CN': '牙买加',
    value: '+1876'
  },
  {
    'en-US': 'Japan',
    'zh-CN': '日本',
    value: '+81'
  },
  {
    'en-US': 'Jordan',
    'zh-CN': '约旦',
    value: '+962'
  },
  {
    'en-US': 'Kampuchea (Cambodia )',
    'zh-CN': '柬埔寨',
    value: '+855'
  },
  {
    'en-US': 'Kazakstan',
    'zh-CN': '哈萨克斯坦',
    value: '+327'
  },
  {
    'en-US': 'Kenya',
    'zh-CN': '肯尼亚',
    value: '+254'
  },
  {
    'en-US': 'Korea',
    'zh-CN': '韩国',
    value: '+82'
  },
  {
    'en-US': 'Kuwait',
    'zh-CN': '科威特',
    value: '+965'
  },
  {
    'en-US': 'Kyrgyzstan',
    'zh-CN': '吉尔吉斯坦',
    value: '+331'
  },
  {
    'en-US': 'Laos',
    'zh-CN': '老挝',
    value: '+856'
  },
  {
    'en-US': 'Latvia',
    'zh-CN': '拉脱维亚',
    value: '+371'
  },
  {
    'en-US': 'Lebanon',
    'zh-CN': '黎巴嫩',
    value: '+961'
  },
  {
    'en-US': 'Lesotho',
    'zh-CN': '莱索托',
    value: '+266'
  },
  {
    'en-US': 'Liberia',
    'zh-CN': '利比里亚',
    value: '+231'
  },
  {
    'en-US': 'Libya',
    'zh-CN': '利比亚',
    value: '+218'
  },
  {
    'en-US': 'Liechtenstein',
    'zh-CN': '列支敦士登',
    value: '+423'
  },
  {
    'en-US': 'Lithuania',
    'zh-CN': '立陶宛',
    value: '+370'
  },
  {
    'en-US': 'Luxembourg',
    'zh-CN': '卢森堡',
    value: '+352'
  },
  {
    'en-US': 'Macao',
    'zh-CN': '澳门',
    value: '+853'
  },
  {
    'en-US': 'Madagascar',
    'zh-CN': '马达加斯加',
    value: '+261'
  },
  {
    'en-US': 'Malawi',
    'zh-CN': '马拉维',
    value: '+265'
  },
  {
    'en-US': 'Malaysia',
    'zh-CN': '马来西亚',
    value: '+60'
  },
  {
    'en-US': 'Maldives',
    'zh-CN': '马尔代夫',
    value: '+960'
  },
  {
    'en-US': 'Mali',
    'zh-CN': '马里',
    value: '+223'
  },
  {
    'en-US': 'Malta',
    'zh-CN': '马耳他',
    value: '+356'
  },
  {
    'en-US': 'Mariana Is',
    'zh-CN': '马里亚那群岛',
    value: '+1670'
  },
  {
    'en-US': 'Martinique',
    'zh-CN': '马提尼克',
    value: '+596'
  },
  {
    'en-US': 'Mauritius',
    'zh-CN': '毛里求斯',
    value: '+230'
  },
  {
    'en-US': 'Mexico',
    'zh-CN': '墨西哥',
    value: '+52'
  },
  {
    'en-US': 'Moldova',
    'zh-CN': '摩尔多瓦',
    value: '+373'
  },
  {
    'en-US': 'Monaco',
    'zh-CN': '摩纳哥',
    value: '+377'
  },
  {
    'en-US': 'Mongolia',
    'zh-CN': '蒙古',
    value: '+976'
  },
  {
    'en-US': 'Montserrat Is',
    'zh-CN': '蒙特塞拉特岛',
    value: '+1664'
  },
  {
    'en-US': 'Morocco',
    'zh-CN': '摩洛哥',
    value: '+212'
  },
  {
    'en-US': 'Mozambique',
    'zh-CN': '莫桑比克',
    value: '+258'
  },
  {
    'en-US': 'Namibia',
    'zh-CN': '纳米比亚',
    value: '+264'
  },
  {
    'en-US': 'Nauru',
    'zh-CN': '瑙鲁',
    value: '+674'
  },
  {
    'en-US': 'Nepal',
    'zh-CN': '尼泊尔',
    value: '+977'
  },
  {
    'en-US': 'Netheriands Antilles',
    'zh-CN': '荷属安的列斯',
    value: '+599'
  },
  {
    'en-US': 'Netherlands',
    'zh-CN': '荷兰',
    value: '+31'
  },
  {
    'en-US': 'New Zealand',
    'zh-CN': '新西兰',
    value: '+64'
  },
  {
    'en-US': 'Nicaragua',
    'zh-CN': '尼加拉瓜',
    value: '+505'
  },
  {
    'en-US': 'Niger',
    'zh-CN': '尼日尔',
    value: '+227'
  },
  {
    'en-US': 'Nigeria',
    'zh-CN': '尼日利亚',
    value: '+234'
  },
  {
    'en-US': 'North Korea',
    'zh-CN': '朝鲜',
    value: '+850'
  },
  {
    'en-US': 'Norway',
    'zh-CN': '挪威',
    value: '+47'
  },
  {
    'en-US': 'Oman',
    'zh-CN': '阿曼',
    value: '+968'
  },
  {
    'en-US': 'Pakistan',
    'zh-CN': '巴基斯坦',
    value: '+92'
  },
  {
    'en-US': 'Panama',
    'zh-CN': '巴拿马',
    value: '+507'
  },
  {
    'en-US': 'Papua New Cuinea',
    'zh-CN': '巴布亚新几内亚',
    value: '+675'
  },
  {
    'en-US': 'Paraguay',
    'zh-CN': '巴拉圭',
    value: '+595'
  },
  {
    'en-US': 'Peru',
    'zh-CN': '秘鲁',
    value: '+51'
  },
  {
    'en-US': 'Philippines',
    'zh-CN': '菲律宾',
    value: '+63'
  },
  {
    'en-US': 'Poland',
    'zh-CN': '波兰',
    value: '+48'
  },
  {
    'en-US': 'Portugal',
    'zh-CN': '葡萄牙',
    value: '+351'
  },
  {
    'en-US': 'Puerto Rico',
    'zh-CN': '波多黎各',
    value: '+1787'
  },
  {
    'en-US': 'Qatar',
    'zh-CN': '卡塔尔',
    value: '+974'
  },
  {
    'en-US': 'Reunion',
    'zh-CN': '留尼旺',
    value: '+262'
  },
  {
    'en-US': 'Romania',
    'zh-CN': '罗马尼亚',
    value: '+40'
  },
  {
    'en-US': 'Russia',
    'zh-CN': '俄罗斯',
    value: '+7'
  },
  {
    'en-US': 'Saint Lueia',
    'zh-CN': '圣卢西亚',
    value: '+1758'
  },
  {
    'en-US': 'Saint Vincent',
    'zh-CN': '圣文森特岛',
    value: '+1784'
  },
  {
    'en-US': 'Samoa Eastern',
    'zh-CN': '东萨摩亚(美)',
    value: '+684'
  },
  {
    'en-US': 'Samoa Western',
    'zh-CN': '西萨摩亚',
    value: '+685'
  },
  {
    'en-US': 'San Marino',
    'zh-CN': '圣马力诺',
    value: '+378'
  },
  {
    'en-US': 'Sao Tome and Principe',
    'zh-CN': '圣多美和普林西比',
    value: '+239'
  },
  {
    'en-US': 'Saudi Arabia',
    'zh-CN': '沙特阿拉伯',
    value: '+966'
  },
  {
    'en-US': 'Senegal',
    'zh-CN': '塞内加尔',
    value: '+221'
  },
  {
    'en-US': 'Seychelles',
    'zh-CN': '塞舌尔',
    value: '+248'
  },
  {
    'en-US': 'Sierra Leone',
    'zh-CN': '塞拉利昂',
    value: '+232'
  },
  {
    'en-US': 'Singapore',
    'zh-CN': '新加坡',
    value: '+65'
  },
  {
    'en-US': 'Slovakia',
    'zh-CN': '斯洛伐克',
    value: '+421'
  },
  {
    'en-US': 'Slovenia',
    'zh-CN': '斯洛文尼亚',
    value: '+386'
  },
  {
    'en-US': 'Solomon Is',
    'zh-CN': '所罗门群岛',
    value: '+677'
  },
  {
    'en-US': 'Somali',
    'zh-CN': '索马里',
    value: '+252'
  },
  {
    'en-US': 'South Africa',
    'zh-CN': '南非',
    value: '+27'
  },
  {
    'en-US': 'Spain',
    'zh-CN': '西班牙',
    value: '+34'
  },
  {
    'en-US': 'SriLanka',
    'zh-CN': '斯里兰卡',
    value: '+94'
  },
  {
    'en-US': 'St.Lucia',
    'zh-CN': '圣卢西亚',
    value: '+1758'
  },
  {
    'en-US': 'St.Vincent',
    'zh-CN': '圣文森特',
    value: '+1784'
  },
  {
    'en-US': 'Sudan',
    'zh-CN': '苏丹',
    value: '+249'
  },
  {
    'en-US': 'Suriname',
    'zh-CN': '苏里南',
    value: '+597'
  },
  {
    'en-US': 'Swaziland',
    'zh-CN': '斯威士兰',
    value: '+268'
  },
  {
    'en-US': 'Sweden',
    'zh-CN': '瑞典',
    value: '+46'
  },
  {
    'en-US': 'Switzerland',
    'zh-CN': '瑞士',
    value: '+41'
  },
  {
    'en-US': 'Syria',
    'zh-CN': '叙利亚',
    value: '+963'
  },
  {
    'en-US': 'Taiwan',
    'zh-CN': '台湾省',
    value: '+886'
  },
  {
    'en-US': 'Tajikstan',
    'zh-CN': '塔吉克斯坦',
    value: '+992'
  },
  {
    'en-US': 'Tanzania',
    'zh-CN': '坦桑尼亚',
    value: '+255'
  },
  {
    'en-US': 'Thailand',
    'zh-CN': '泰国',
    value: '+66'
  },
  {
    'en-US': 'Togo',
    'zh-CN': '多哥',
    value: '+228'
  },
  {
    'en-US': 'Tonga',
    'zh-CN': '汤加',
    value: '+676'
  },
  {
    'en-US': 'Trinidad and Tobago',
    'zh-CN': '特立尼达和多巴哥',
    value: '+1809'
  },
  {
    'en-US': 'Tunisia',
    'zh-CN': '突尼斯',
    value: '+216'
  },
  {
    'en-US': 'Turkey',
    'zh-CN': '土耳其',
    value: '+90'
  },
  {
    'en-US': 'Turkmenistan',
    'zh-CN': '土库曼斯坦',
    value: '+993'
  },
  {
    'en-US': 'Uganda',
    'zh-CN': '乌干达',
    value: '+256'
  },
  {
    'en-US': 'Ukraine',
    'zh-CN': '乌克兰',
    value: '+380'
  },
  {
    'en-US': 'United Arab Emirates',
    'zh-CN': '阿拉伯联合酋长国',
    value: '+971'
  },
  {
    'en-US': 'United Kiongdom',
    'zh-CN': '英国',
    value: '+44'
  },
  {
    'en-US': 'United States of America',
    'zh-CN': '美国',
    value: '+1'
  },
  {
    'en-US': 'Uruguay',
    'zh-CN': '乌拉圭',
    value: '+598'
  },
  {
    'en-US': 'Uzbekistan',
    'zh-CN': '乌兹别克斯坦',
    value: '+233'
  },
  {
    'en-US': 'Venezuela',
    'zh-CN': '委内瑞拉',
    value: '+58'
  },
  {
    'en-US': 'Vietnam',
    'zh-CN': '越南',
    value: '+84'
  },
  {
    'en-US': 'Yemen',
    'zh-CN': '也门',
    value: '+967'
  },
  {
    'en-US': 'Yugoslavia',
    'zh-CN': '南斯拉夫',
    value: '+381'
  },
  {
    'en-US': 'Zimbabwe',
    'zh-CN': '津巴布韦',
    value: '+263'
  },
  {
    'en-US': 'Zaire',
    'zh-CN': '扎伊尔',
    value: '+243'
  },
  {
    'en-US': 'Zambia',
    'zh-CN': '赞比亚',
    value: '+260'
  }
];

export default function (language: SUPPORT_LANGUAGES) {
  const ac = [];
  for (const areaCode of areaCodes) {
    ac.push({
      label: areaCode.value + ' (' + areaCode[language] + ')',
      value: areaCode.value
    });
  }
  return ac;
}
