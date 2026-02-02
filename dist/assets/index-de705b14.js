(function(){const e=document.createElement("link").relList;if(e&&e.supports&&e.supports("modulepreload"))return;for(const o of document.querySelectorAll('link[rel="modulepreload"]'))r(o);new MutationObserver(o=>{for(const s of o)if(s.type==="childList")for(const i of s.addedNodes)i.tagName==="LINK"&&i.rel==="modulepreload"&&r(i)}).observe(document,{childList:!0,subtree:!0});function t(o){const s={};return o.integrity&&(s.integrity=o.integrity),o.referrerPolicy&&(s.referrerPolicy=o.referrerPolicy),o.crossOrigin==="use-credentials"?s.credentials="include":o.crossOrigin==="anonymous"?s.credentials="omit":s.credentials="same-origin",s}function r(o){if(o.ep)return;o.ep=!0;const s=t(o);fetch(o.href,s)}})();/**
* @vue/shared v3.5.27
* (c) 2018-present Yuxi (Evan) You and Vue contributors
* @license MIT
**/function ar(n){const e=Object.create(null);for(const t of n.split(","))e[t]=1;return t=>t in e}const Z={},_e=[],Tn=()=>{},Co=()=>!1,xt=n=>n.charCodeAt(0)===111&&n.charCodeAt(1)===110&&(n.charCodeAt(2)>122||n.charCodeAt(2)<97),lr=n=>n.startsWith("onUpdate:"),on=Object.assign,cr=(n,e)=>{const t=n.indexOf(e);t>-1&&n.splice(t,1)},qs=Object.prototype.hasOwnProperty,G=(n,e)=>qs.call(n,e),H=Array.isArray,Fe=n=>wt(n)==="[object Map]",Gs=n=>wt(n)==="[object Set]",j=n=>typeof n=="function",an=n=>typeof n=="string",Ne=n=>typeof n=="symbol",tn=n=>n!==null&&typeof n=="object",_o=n=>(tn(n)||j(n))&&j(n.then)&&j(n.catch),Ks=Object.prototype.toString,wt=n=>Ks.call(n),Ws=n=>wt(n).slice(8,-1),Js=n=>wt(n)==="[object Object]",dr=n=>an(n)&&n!=="NaN"&&n[0]!=="-"&&""+parseInt(n,10)===n,ze=ar(",key,ref,ref_for,ref_key,onVnodeBeforeMount,onVnodeMounted,onVnodeBeforeUpdate,onVnodeUpdated,onVnodeBeforeUnmount,onVnodeUnmounted"),Et=n=>{const e=Object.create(null);return t=>e[t]||(e[t]=n(t))},Ys=/-\w/g,_n=Et(n=>n.replace(Ys,e=>e.slice(1).toUpperCase())),Qs=/\B([A-Z])/g,ve=Et(n=>n.replace(Qs,"-$1").toLowerCase()),kt=Et(n=>n.charAt(0).toUpperCase()+n.slice(1)),Pt=Et(n=>n?`on${kt(n)}`:""),ce=(n,e)=>!Object.is(n,e),At=(n,...e)=>{for(let t=0;t<n.length;t++)n[t](...e)},Io=(n,e,t,r=!1)=>{Object.defineProperty(n,e,{configurable:!0,enumerable:!1,writable:r,value:t})},Zs=n=>{const e=parseFloat(n);return isNaN(e)?n:e};let Or;const Ct=()=>Or||(Or=typeof globalThis<"u"?globalThis:typeof self<"u"?self:typeof window<"u"?window:typeof global<"u"?global:{});function ur(n){if(H(n)){const e={};for(let t=0;t<n.length;t++){const r=n[t],o=an(r)?ti(r):ur(r);if(o)for(const s in o)e[s]=o[s]}return e}else if(an(n)||tn(n))return n}const Xs=/;(?![^(]*\))/g,ni=/:([^]+)/,ei=/\/\*[^]*?\*\//g;function ti(n){const e={};return n.replace(ei,"").split(Xs).forEach(t=>{if(t){const r=t.split(ni);r.length>1&&(e[r[0].trim()]=r[1].trim())}}),e}function pr(n){let e="";if(an(n))e=n;else if(H(n))for(let t=0;t<n.length;t++){const r=pr(n[t]);r&&(e+=r+" ")}else if(tn(n))for(const t in n)n[t]&&(e+=t+" ");return e.trim()}const ri="itemscope,allowfullscreen,formnovalidate,ismap,nomodule,novalidate,readonly",oi=ar(ri);function So(n){return!!n||n===""}/**
* @vue/reactivity v3.5.27
* (c) 2018-present Yuxi (Evan) You and Vue contributors
* @license MIT
**/let vn;class si{constructor(e=!1){this.detached=e,this._active=!0,this._on=0,this.effects=[],this.cleanups=[],this._isPaused=!1,this.parent=vn,!e&&vn&&(this.index=(vn.scopes||(vn.scopes=[])).push(this)-1)}get active(){return this._active}pause(){if(this._active){this._isPaused=!0;let e,t;if(this.scopes)for(e=0,t=this.scopes.length;e<t;e++)this.scopes[e].pause();for(e=0,t=this.effects.length;e<t;e++)this.effects[e].pause()}}resume(){if(this._active&&this._isPaused){this._isPaused=!1;let e,t;if(this.scopes)for(e=0,t=this.scopes.length;e<t;e++)this.scopes[e].resume();for(e=0,t=this.effects.length;e<t;e++)this.effects[e].resume()}}run(e){if(this._active){const t=vn;try{return vn=this,e()}finally{vn=t}}}on(){++this._on===1&&(this.prevScope=vn,vn=this)}off(){this._on>0&&--this._on===0&&(vn=this.prevScope,this.prevScope=void 0)}stop(e){if(this._active){this._active=!1;let t,r;for(t=0,r=this.effects.length;t<r;t++)this.effects[t].stop();for(this.effects.length=0,t=0,r=this.cleanups.length;t<r;t++)this.cleanups[t]();if(this.cleanups.length=0,this.scopes){for(t=0,r=this.scopes.length;t<r;t++)this.scopes[t].stop(!0);this.scopes.length=0}if(!this.detached&&this.parent&&!e){const o=this.parent.scopes.pop();o&&o!==this&&(this.parent.scopes[this.index]=o,o.index=this.index)}this.parent=void 0}}}function ii(){return vn}let Q;const Ot=new WeakSet;class Bo{constructor(e){this.fn=e,this.deps=void 0,this.depsTail=void 0,this.flags=5,this.next=void 0,this.cleanup=void 0,this.scheduler=void 0,vn&&vn.active&&vn.effects.push(this)}pause(){this.flags|=64}resume(){this.flags&64&&(this.flags&=-65,Ot.has(this)&&(Ot.delete(this),this.trigger()))}notify(){this.flags&2&&!(this.flags&32)||this.flags&8||Lo(this)}run(){if(!(this.flags&1))return this.fn();this.flags|=2,Nr(this),Po(this);const e=Q,t=Ln;Q=this,Ln=!0;try{return this.fn()}finally{Ao(this),Q=e,Ln=t,this.flags&=-3}}stop(){if(this.flags&1){for(let e=this.deps;e;e=e.nextDep)gr(e);this.deps=this.depsTail=void 0,Nr(this),this.onStop&&this.onStop(),this.flags&=-2}}trigger(){this.flags&64?Ot.add(this):this.scheduler?this.scheduler():this.runIfDirty()}runIfDirty(){Gt(this)&&this.run()}get dirty(){return Gt(this)}}let To=0,$e,Ve;function Lo(n,e=!1){if(n.flags|=8,e){n.next=Ve,Ve=n;return}n.next=$e,$e=n}function fr(){To++}function mr(){if(--To>0)return;if(Ve){let e=Ve;for(Ve=void 0;e;){const t=e.next;e.next=void 0,e.flags&=-9,e=t}}let n;for(;$e;){let e=$e;for($e=void 0;e;){const t=e.next;if(e.next=void 0,e.flags&=-9,e.flags&1)try{e.trigger()}catch(r){n||(n=r)}e=t}}if(n)throw n}function Po(n){for(let e=n.deps;e;e=e.nextDep)e.version=-1,e.prevActiveLink=e.dep.activeLink,e.dep.activeLink=e}function Ao(n){let e,t=n.depsTail,r=t;for(;r;){const o=r.prevDep;r.version===-1?(r===t&&(t=o),gr(r),ai(r)):e=r,r.dep.activeLink=r.prevActiveLink,r.prevActiveLink=void 0,r=o}n.deps=e,n.depsTail=t}function Gt(n){for(let e=n.deps;e;e=e.nextDep)if(e.dep.version!==e.version||e.dep.computed&&(Oo(e.dep.computed)||e.dep.version!==e.version))return!0;return!!n._dirty}function Oo(n){if(n.flags&4&&!(n.flags&16)||(n.flags&=-17,n.globalVersion===Ye)||(n.globalVersion=Ye,!n.isSSR&&n.flags&128&&(!n.deps&&!n._dirty||!Gt(n))))return;n.flags|=2;const e=n.dep,t=Q,r=Ln;Q=n,Ln=!0;try{Po(n);const o=n.fn(n._value);(e.version===0||ce(o,n._value))&&(n.flags|=128,n._value=o,e.version++)}catch(o){throw e.version++,o}finally{Q=t,Ln=r,Ao(n),n.flags&=-3}}function gr(n,e=!1){const{dep:t,prevSub:r,nextSub:o}=n;if(r&&(r.nextSub=o,n.prevSub=void 0),o&&(o.prevSub=r,n.nextSub=void 0),t.subs===n&&(t.subs=r,!r&&t.computed)){t.computed.flags&=-5;for(let s=t.computed.deps;s;s=s.nextDep)gr(s,!0)}!e&&!--t.sc&&t.map&&t.map.delete(t.key)}function ai(n){const{prevDep:e,nextDep:t}=n;e&&(e.nextDep=t,n.prevDep=void 0),t&&(t.prevDep=e,n.nextDep=void 0)}let Ln=!0;const No=[];function Yn(){No.push(Ln),Ln=!1}function Qn(){const n=No.pop();Ln=n===void 0?!0:n}function Nr(n){const{cleanup:e}=n;if(n.cleanup=void 0,e){const t=Q;Q=void 0;try{e()}finally{Q=t}}}let Ye=0;class li{constructor(e,t){this.sub=e,this.dep=t,this.version=t.version,this.nextDep=this.prevDep=this.nextSub=this.prevSub=this.prevActiveLink=void 0}}class br{constructor(e){this.computed=e,this.version=0,this.activeLink=void 0,this.subs=void 0,this.map=void 0,this.key=void 0,this.sc=0,this.__v_skip=!0}track(e){if(!Q||!Ln||Q===this.computed)return;let t=this.activeLink;if(t===void 0||t.sub!==Q)t=this.activeLink=new li(Q,this),Q.deps?(t.prevDep=Q.depsTail,Q.depsTail.nextDep=t,Q.depsTail=t):Q.deps=Q.depsTail=t,Ro(t);else if(t.version===-1&&(t.version=this.version,t.nextDep)){const r=t.nextDep;r.prevDep=t.prevDep,t.prevDep&&(t.prevDep.nextDep=r),t.prevDep=Q.depsTail,t.nextDep=void 0,Q.depsTail.nextDep=t,Q.depsTail=t,Q.deps===t&&(Q.deps=r)}return t}trigger(e){this.version++,Ye++,this.notify(e)}notify(e){fr();try{for(let t=this.subs;t;t=t.prevSub)t.sub.notify()&&t.sub.dep.notify()}finally{mr()}}}function Ro(n){if(n.dep.sc++,n.sub.flags&4){const e=n.dep.computed;if(e&&!n.dep.subs){e.flags|=20;for(let r=e.deps;r;r=r.nextDep)Ro(r)}const t=n.dep.subs;t!==n&&(n.prevSub=t,t&&(t.nextSub=n)),n.dep.subs=n}}const Kt=new WeakMap,he=Symbol(""),Wt=Symbol(""),Qe=Symbol("");function dn(n,e,t){if(Ln&&Q){let r=Kt.get(n);r||Kt.set(n,r=new Map);let o=r.get(t);o||(r.set(t,o=new br),o.map=r,o.key=t),o.track()}}function Wn(n,e,t,r,o,s){const i=Kt.get(n);if(!i){Ye++;return}const a=l=>{l&&l.trigger()};if(fr(),e==="clear")i.forEach(a);else{const l=H(n),f=l&&dr(t);if(l&&t==="length"){const u=Number(r);i.forEach((p,m)=>{(m==="length"||m===Qe||!Ne(m)&&m>=u)&&a(p)})}else switch((t!==void 0||i.has(void 0))&&a(i.get(t)),f&&a(i.get(Qe)),e){case"add":l?f&&a(i.get("length")):(a(i.get(he)),Fe(n)&&a(i.get(Wt)));break;case"delete":l||(a(i.get(he)),Fe(n)&&a(i.get(Wt)));break;case"set":Fe(n)&&a(i.get(he));break}}mr()}function Ee(n){const e=q(n);return e===n?e:(dn(e,"iterate",Qe),Pn(n)?e:e.map(Zn))}function hr(n){return dn(n=q(n),"iterate",Qe),n}function oe(n,e){return de(n)?Ze(Ie(n)?Zn(e):e):Zn(e)}const ci={__proto__:null,[Symbol.iterator](){return Nt(this,Symbol.iterator,n=>oe(this,n))},concat(...n){return Ee(this).concat(...n.map(e=>H(e)?Ee(e):e))},entries(){return Nt(this,"entries",n=>(n[1]=oe(this,n[1]),n))},every(n,e){return qn(this,"every",n,e,void 0,arguments)},filter(n,e){return qn(this,"filter",n,e,t=>t.map(r=>oe(this,r)),arguments)},find(n,e){return qn(this,"find",n,e,t=>oe(this,t),arguments)},findIndex(n,e){return qn(this,"findIndex",n,e,void 0,arguments)},findLast(n,e){return qn(this,"findLast",n,e,t=>oe(this,t),arguments)},findLastIndex(n,e){return qn(this,"findLastIndex",n,e,void 0,arguments)},forEach(n,e){return qn(this,"forEach",n,e,void 0,arguments)},includes(...n){return Rt(this,"includes",n)},indexOf(...n){return Rt(this,"indexOf",n)},join(n){return Ee(this).join(n)},lastIndexOf(...n){return Rt(this,"lastIndexOf",n)},map(n,e){return qn(this,"map",n,e,void 0,arguments)},pop(){return De(this,"pop")},push(...n){return De(this,"push",n)},reduce(n,...e){return Rr(this,"reduce",n,e)},reduceRight(n,...e){return Rr(this,"reduceRight",n,e)},shift(){return De(this,"shift")},some(n,e){return qn(this,"some",n,e,void 0,arguments)},splice(...n){return De(this,"splice",n)},toReversed(){return Ee(this).toReversed()},toSorted(n){return Ee(this).toSorted(n)},toSpliced(...n){return Ee(this).toSpliced(...n)},unshift(...n){return De(this,"unshift",n)},values(){return Nt(this,"values",n=>oe(this,n))}};function Nt(n,e,t){const r=hr(n),o=r[e]();return r!==n&&!Pn(n)&&(o._next=o.next,o.next=()=>{const s=o._next();return s.done||(s.value=t(s.value)),s}),o}const di=Array.prototype;function qn(n,e,t,r,o,s){const i=hr(n),a=i!==n&&!Pn(n),l=i[e];if(l!==di[e]){const p=l.apply(n,s);return a?Zn(p):p}let f=t;i!==n&&(a?f=function(p,m){return t.call(this,oe(n,p),m,n)}:t.length>2&&(f=function(p,m){return t.call(this,p,m,n)}));const u=l.call(i,f,r);return a&&o?o(u):u}function Rr(n,e,t,r){const o=hr(n);let s=t;return o!==n&&(Pn(n)?t.length>3&&(s=function(i,a,l){return t.call(this,i,a,l,n)}):s=function(i,a,l){return t.call(this,i,oe(n,a),l,n)}),o[e](s,...r)}function Rt(n,e,t){const r=q(n);dn(r,"iterate",Qe);const o=r[e](...t);return(o===-1||o===!1)&&xr(t[0])?(t[0]=q(t[0]),r[e](...t)):o}function De(n,e,t=[]){Yn(),fr();const r=q(n)[e].apply(n,t);return mr(),Qn(),r}const ui=ar("__proto__,__v_isRef,__isVue"),Mo=new Set(Object.getOwnPropertyNames(Symbol).filter(n=>n!=="arguments"&&n!=="caller").map(n=>Symbol[n]).filter(Ne));function pi(n){Ne(n)||(n=String(n));const e=q(this);return dn(e,"has",n),e.hasOwnProperty(n)}class Do{constructor(e=!1,t=!1){this._isReadonly=e,this._isShallow=t}get(e,t,r){if(t==="__v_skip")return e.__v_skip;const o=this._isReadonly,s=this._isShallow;if(t==="__v_isReactive")return!o;if(t==="__v_isReadonly")return o;if(t==="__v_isShallow")return s;if(t==="__v_raw")return r===(o?s?Ei:Fo:s?Ho:jo).get(e)||Object.getPrototypeOf(e)===Object.getPrototypeOf(r)?e:void 0;const i=H(e);if(!o){let l;if(i&&(l=ci[t]))return l;if(t==="hasOwnProperty")return pi}const a=Reflect.get(e,t,pn(e)?e:r);if((Ne(t)?Mo.has(t):ui(t))||(o||dn(e,"get",t),s))return a;if(pn(a)){const l=i&&dr(t)?a:a.value;return o&&tn(l)?Yt(l):l}return tn(a)?o?Yt(a):_t(a):a}}class Uo extends Do{constructor(e=!1){super(!1,e)}set(e,t,r,o){let s=e[t];const i=H(e)&&dr(t);if(!this._isShallow){const f=de(s);if(!Pn(r)&&!de(r)&&(s=q(s),r=q(r)),!i&&pn(s)&&!pn(r))return f||(s.value=r),!0}const a=i?Number(t)<e.length:G(e,t),l=Reflect.set(e,t,r,pn(e)?e:o);return e===q(o)&&(a?ce(r,s)&&Wn(e,"set",t,r):Wn(e,"add",t,r)),l}deleteProperty(e,t){const r=G(e,t);e[t];const o=Reflect.deleteProperty(e,t);return o&&r&&Wn(e,"delete",t,void 0),o}has(e,t){const r=Reflect.has(e,t);return(!Ne(t)||!Mo.has(t))&&dn(e,"has",t),r}ownKeys(e){return dn(e,"iterate",H(e)?"length":he),Reflect.ownKeys(e)}}class fi extends Do{constructor(e=!1){super(!0,e)}set(e,t){return!0}deleteProperty(e,t){return!0}}const mi=new Uo,gi=new fi,bi=new Uo(!0);const Jt=n=>n,st=n=>Reflect.getPrototypeOf(n);function hi(n,e,t){return function(...r){const o=this.__v_raw,s=q(o),i=Fe(s),a=n==="entries"||n===Symbol.iterator&&i,l=n==="keys"&&i,f=o[n](...r),u=t?Jt:e?Ze:Zn;return!e&&dn(s,"iterate",l?Wt:he),on(Object.create(f),{next(){const{value:p,done:m}=f.next();return m?{value:p,done:m}:{value:a?[u(p[0]),u(p[1])]:u(p),done:m}}})}}function it(n){return function(...e){return n==="delete"?!1:n==="clear"?void 0:this}}function vi(n,e){const t={get(o){const s=this.__v_raw,i=q(s),a=q(o);n||(ce(o,a)&&dn(i,"get",o),dn(i,"get",a));const{has:l}=st(i),f=e?Jt:n?Ze:Zn;if(l.call(i,o))return f(s.get(o));if(l.call(i,a))return f(s.get(a));s!==i&&s.get(o)},get size(){const o=this.__v_raw;return!n&&dn(q(o),"iterate",he),o.size},has(o){const s=this.__v_raw,i=q(s),a=q(o);return n||(ce(o,a)&&dn(i,"has",o),dn(i,"has",a)),o===a?s.has(o):s.has(o)||s.has(a)},forEach(o,s){const i=this,a=i.__v_raw,l=q(a),f=e?Jt:n?Ze:Zn;return!n&&dn(l,"iterate",he),a.forEach((u,p)=>o.call(s,f(u),f(p),i))}};return on(t,n?{add:it("add"),set:it("set"),delete:it("delete"),clear:it("clear")}:{add(o){!e&&!Pn(o)&&!de(o)&&(o=q(o));const s=q(this);return st(s).has.call(s,o)||(s.add(o),Wn(s,"add",o,o)),this},set(o,s){!e&&!Pn(s)&&!de(s)&&(s=q(s));const i=q(this),{has:a,get:l}=st(i);let f=a.call(i,o);f||(o=q(o),f=a.call(i,o));const u=l.call(i,o);return i.set(o,s),f?ce(s,u)&&Wn(i,"set",o,s):Wn(i,"add",o,s),this},delete(o){const s=q(this),{has:i,get:a}=st(s);let l=i.call(s,o);l||(o=q(o),l=i.call(s,o)),a&&a.call(s,o);const f=s.delete(o);return l&&Wn(s,"delete",o,void 0),f},clear(){const o=q(this),s=o.size!==0,i=o.clear();return s&&Wn(o,"clear",void 0,void 0),i}}),["keys","values","entries",Symbol.iterator].forEach(o=>{t[o]=hi(o,n,e)}),t}function vr(n,e){const t=vi(n,e);return(r,o,s)=>o==="__v_isReactive"?!n:o==="__v_isReadonly"?n:o==="__v_raw"?r:Reflect.get(G(t,o)&&o in r?t:r,o,s)}const yi={get:vr(!1,!1)},xi={get:vr(!1,!0)},wi={get:vr(!0,!1)};const jo=new WeakMap,Ho=new WeakMap,Fo=new WeakMap,Ei=new WeakMap;function ki(n){switch(n){case"Object":case"Array":return 1;case"Map":case"Set":case"WeakMap":case"WeakSet":return 2;default:return 0}}function Ci(n){return n.__v_skip||!Object.isExtensible(n)?0:ki(Ws(n))}function _t(n){return de(n)?n:yr(n,!1,mi,yi,jo)}function zo(n){return yr(n,!1,bi,xi,Ho)}function Yt(n){return yr(n,!0,gi,wi,Fo)}function yr(n,e,t,r,o){if(!tn(n)||n.__v_raw&&!(e&&n.__v_isReactive))return n;const s=Ci(n);if(s===0)return n;const i=o.get(n);if(i)return i;const a=new Proxy(n,s===2?r:t);return o.set(n,a),a}function Ie(n){return de(n)?Ie(n.__v_raw):!!(n&&n.__v_isReactive)}function de(n){return!!(n&&n.__v_isReadonly)}function Pn(n){return!!(n&&n.__v_isShallow)}function xr(n){return n?!!n.__v_raw:!1}function q(n){const e=n&&n.__v_raw;return e?q(e):n}function _i(n){return!G(n,"__v_skip")&&Object.isExtensible(n)&&Io(n,"__v_skip",!0),n}const Zn=n=>tn(n)?_t(n):n,Ze=n=>tn(n)?Yt(n):n;function pn(n){return n?n.__v_isRef===!0:!1}function $o(n){return Vo(n,!1)}function Ii(n){return Vo(n,!0)}function Vo(n,e){return pn(n)?n:new Si(n,e)}class Si{constructor(e,t){this.dep=new br,this.__v_isRef=!0,this.__v_isShallow=!1,this._rawValue=t?e:q(e),this._value=t?e:Zn(e),this.__v_isShallow=t}get value(){return this.dep.track(),this._value}set value(e){const t=this._rawValue,r=this.__v_isShallow||Pn(e)||de(e);e=r?e:q(e),ce(e,t)&&(this._rawValue=e,this._value=r?e:Zn(e),this.dep.trigger())}}function Se(n){return pn(n)?n.value:n}const Bi={get:(n,e,t)=>e==="__v_raw"?n:Se(Reflect.get(n,e,t)),set:(n,e,t,r)=>{const o=n[e];return pn(o)&&!pn(t)?(o.value=t,!0):Reflect.set(n,e,t,r)}};function qo(n){return Ie(n)?n:new Proxy(n,Bi)}class Ti{constructor(e,t,r){this.fn=e,this.setter=t,this._value=void 0,this.dep=new br(this),this.__v_isRef=!0,this.deps=void 0,this.depsTail=void 0,this.flags=16,this.globalVersion=Ye-1,this.next=void 0,this.effect=this,this.__v_isReadonly=!t,this.isSSR=r}notify(){if(this.flags|=16,!(this.flags&8)&&Q!==this)return Lo(this,!0),!0}get value(){const e=this.dep.track();return Oo(this),e&&(e.version=this.dep.version),this._value}set value(e){this.setter&&this.setter(e)}}function Li(n,e,t=!1){let r,o;return j(n)?r=n:(r=n.get,o=n.set),new Ti(r,o,t)}const at={},pt=new WeakMap;let ge;function Pi(n,e=!1,t=ge){if(t){let r=pt.get(t);r||pt.set(t,r=[]),r.push(n)}}function Ai(n,e,t=Z){const{immediate:r,deep:o,once:s,scheduler:i,augmentJob:a,call:l}=t,f=P=>o?P:Pn(P)||o===!1||o===0?le(P,1):le(P);let u,p,m,b,_=!1,B=!1;if(pn(n)?(p=()=>n.value,_=Pn(n)):Ie(n)?(p=()=>f(n),_=!0):H(n)?(B=!0,_=n.some(P=>Ie(P)||Pn(P)),p=()=>n.map(P=>{if(pn(P))return P.value;if(Ie(P))return f(P);if(j(P))return l?l(P,2):P()})):j(n)?e?p=l?()=>l(n,2):n:p=()=>{if(m){Yn();try{m()}finally{Qn()}}const P=ge;ge=u;try{return l?l(n,3,[b]):n(b)}finally{ge=P}}:p=Tn,e&&o){const P=p,J=o===!0?1/0:o;p=()=>le(P(),J)}const U=ii(),O=()=>{u.stop(),U&&U.active&&cr(U.effects,u)};if(s&&e){const P=e;e=(...J)=>{P(...J),O()}}let L=B?new Array(n.length).fill(at):at;const R=P=>{if(!(!(u.flags&1)||!u.dirty&&!P))if(e){const J=u.run();if(o||_||(B?J.some((ln,nn)=>ce(ln,L[nn])):ce(J,L))){m&&m();const ln=ge;ge=u;try{const nn=[J,L===at?void 0:B&&L[0]===at?[]:L,b];L=J,l?l(e,3,nn):e(...nn)}finally{ge=ln}}}else u.run()};return a&&a(R),u=new Bo(p),u.scheduler=i?()=>i(R,!1):R,b=P=>Pi(P,!1,u),m=u.onStop=()=>{const P=pt.get(u);if(P){if(l)l(P,4);else for(const J of P)J();pt.delete(u)}},e?r?R(!0):L=u.run():i?i(R.bind(null,!0),!0):u.run(),O.pause=u.pause.bind(u),O.resume=u.resume.bind(u),O.stop=O,O}function le(n,e=1/0,t){if(e<=0||!tn(n)||n.__v_skip||(t=t||new Map,(t.get(n)||0)>=e))return n;if(t.set(n,e),e--,pn(n))le(n.value,e,t);else if(H(n))for(let r=0;r<n.length;r++)le(n[r],e,t);else if(Gs(n)||Fe(n))n.forEach(r=>{le(r,e,t)});else if(Js(n)){for(const r in n)le(n[r],e,t);for(const r of Object.getOwnPropertySymbols(n))Object.prototype.propertyIsEnumerable.call(n,r)&&le(n[r],e,t)}return n}/**
* @vue/runtime-core v3.5.27
* (c) 2018-present Yuxi (Evan) You and Vue contributors
* @license MIT
**/function rt(n,e,t,r){try{return r?n(...r):n()}catch(o){It(o,e,t)}}function $n(n,e,t,r){if(j(n)){const o=rt(n,e,t,r);return o&&_o(o)&&o.catch(s=>{It(s,e,t)}),o}if(H(n)){const o=[];for(let s=0;s<n.length;s++)o.push($n(n[s],e,t,r));return o}}function It(n,e,t,r=!0){const o=e?e.vnode:null,{errorHandler:s,throwUnhandledErrorInProduction:i}=e&&e.appContext.config||Z;if(e){let a=e.parent;const l=e.proxy,f=`https://vuejs.org/error-reference/#runtime-${t}`;for(;a;){const u=a.ec;if(u){for(let p=0;p<u.length;p++)if(u[p](n,l,f)===!1)return}a=a.parent}if(s){Yn(),rt(s,null,10,[n,l,f]),Qn();return}}Oi(n,t,o,r,i)}function Oi(n,e,t,r=!0,o=!1){if(o)throw n;console.error(n)}const gn=[];let Hn=-1;const Be=[];let se=null,ke=0;const Go=Promise.resolve();let ft=null;function Ko(n){const e=ft||Go;return n?e.then(this?n.bind(this):n):e}function Ni(n){let e=Hn+1,t=gn.length;for(;e<t;){const r=e+t>>>1,o=gn[r],s=Xe(o);s<n||s===n&&o.flags&2?e=r+1:t=r}return e}function wr(n){if(!(n.flags&1)){const e=Xe(n),t=gn[gn.length-1];!t||!(n.flags&2)&&e>=Xe(t)?gn.push(n):gn.splice(Ni(e),0,n),n.flags|=1,Wo()}}function Wo(){ft||(ft=Go.then(Yo))}function Ri(n){H(n)?Be.push(...n):se&&n.id===-1?se.splice(ke+1,0,n):n.flags&1||(Be.push(n),n.flags|=1),Wo()}function Mr(n,e,t=Hn+1){for(;t<gn.length;t++){const r=gn[t];if(r&&r.flags&2){if(n&&r.id!==n.uid)continue;gn.splice(t,1),t--,r.flags&4&&(r.flags&=-2),r(),r.flags&4||(r.flags&=-2)}}}function Jo(n){if(Be.length){const e=[...new Set(Be)].sort((t,r)=>Xe(t)-Xe(r));if(Be.length=0,se){se.push(...e);return}for(se=e,ke=0;ke<se.length;ke++){const t=se[ke];t.flags&4&&(t.flags&=-2),t.flags&8||t(),t.flags&=-2}se=null,ke=0}}const Xe=n=>n.id==null?n.flags&2?-1:1/0:n.id;function Yo(n){const e=Tn;try{for(Hn=0;Hn<gn.length;Hn++){const t=gn[Hn];t&&!(t.flags&8)&&(t.flags&4&&(t.flags&=-2),rt(t,t.i,t.i?15:14),t.flags&4||(t.flags&=-2))}}finally{for(;Hn<gn.length;Hn++){const t=gn[Hn];t&&(t.flags&=-2)}Hn=-1,gn.length=0,Jo(),ft=null,(gn.length||Be.length)&&Yo()}}let Bn=null,Qo=null;function mt(n){const e=Bn;return Bn=n,Qo=n&&n.type.__scopeId||null,e}function Mi(n,e=Bn,t){if(!e||n._n)return n;const r=(...o)=>{r._d&&ht(-1);const s=mt(e);let i;try{i=n(...o)}finally{mt(s),r._d&&ht(1)}return i};return r._n=!0,r._c=!0,r._d=!0,r}function fe(n,e,t,r){const o=n.dirs,s=e&&e.dirs;for(let i=0;i<o.length;i++){const a=o[i];s&&(a.oldValue=s[i].value);let l=a.dir[r];l&&(Yn(),$n(l,t,8,[n.el,a,n,e]),Qn())}}function lt(n,e){if(un){let t=un.provides;const r=un.parent&&un.parent.provides;r===t&&(t=un.provides=Object.create(r)),t[n]=e}}function Jn(n,e,t=!1){const r=ja();if(r||Te){let o=Te?Te._context.provides:r?r.parent==null||r.ce?r.vnode.appContext&&r.vnode.appContext.provides:r.parent.provides:void 0;if(o&&n in o)return o[n];if(arguments.length>1)return t&&j(e)?e.call(r&&r.proxy):e}}const Di=Symbol.for("v-scx"),Ui=()=>Jn(Di);function ct(n,e,t){return Zo(n,e,t)}function Zo(n,e,t=Z){const{immediate:r,deep:o,flush:s,once:i}=t,a=on({},t),l=e&&r||!e&&s!=="post";let f;if(et){if(s==="sync"){const b=Ui();f=b.__watcherHandles||(b.__watcherHandles=[])}else if(!l){const b=()=>{};return b.stop=Tn,b.resume=Tn,b.pause=Tn,b}}const u=un;a.call=(b,_,B)=>$n(b,u,_,B);let p=!1;s==="post"?a.scheduler=b=>{wn(b,u&&u.suspense)}:s!=="sync"&&(p=!0,a.scheduler=(b,_)=>{_?b():wr(b)}),a.augmentJob=b=>{e&&(b.flags|=4),p&&(b.flags|=2,u&&(b.id=u.uid,b.i=u))};const m=Ai(n,e,a);return et&&(f?f.push(m):l&&m()),m}function ji(n,e,t){const r=this.proxy,o=an(n)?n.includes(".")?Xo(r,n):()=>r[n]:n.bind(r,r);let s;j(e)?s=e:(s=e.handler,t=e);const i=ot(this),a=Zo(o,s.bind(r),t);return i(),a}function Xo(n,e){const t=e.split(".");return()=>{let r=n;for(let o=0;o<t.length&&r;o++)r=r[t[o]];return r}}const Hi=Symbol("_vte"),Fi=n=>n.__isTeleport,zi=Symbol("_leaveCb");function Er(n,e){n.shapeFlag&6&&n.component?(n.transition=e,Er(n.component.subTree,e)):n.shapeFlag&128?(n.ssContent.transition=e.clone(n.ssContent),n.ssFallback.transition=e.clone(n.ssFallback)):n.transition=e}function ns(n,e){return j(n)?(()=>on({name:n.name},e,{setup:n}))():n}function es(n){n.ids=[n.ids[0]+n.ids[2]+++"-",0,0]}const gt=new WeakMap;function qe(n,e,t,r,o=!1){if(H(n)){n.forEach((_,B)=>qe(_,e&&(H(e)?e[B]:e),t,r,o));return}if(Ge(r)&&!o){r.shapeFlag&512&&r.type.__asyncResolved&&r.component.subTree.component&&qe(n,e,t,r.component.subTree);return}const s=r.shapeFlag&4?Sr(r.component):r.el,i=o?null:s,{i:a,r:l}=n,f=e&&e.r,u=a.refs===Z?a.refs={}:a.refs,p=a.setupState,m=q(p),b=p===Z?Co:_=>G(m,_);if(f!=null&&f!==l){if(Dr(e),an(f))u[f]=null,b(f)&&(p[f]=null);else if(pn(f)){f.value=null;const _=e;_.k&&(u[_.k]=null)}}if(j(l))rt(l,a,12,[i,u]);else{const _=an(l),B=pn(l);if(_||B){const U=()=>{if(n.f){const O=_?b(l)?p[l]:u[l]:l.value;if(o)H(O)&&cr(O,s);else if(H(O))O.includes(s)||O.push(s);else if(_)u[l]=[s],b(l)&&(p[l]=u[l]);else{const L=[s];l.value=L,n.k&&(u[n.k]=L)}}else _?(u[l]=i,b(l)&&(p[l]=i)):B&&(l.value=i,n.k&&(u[n.k]=i))};if(i){const O=()=>{U(),gt.delete(n)};O.id=-1,gt.set(n,O),wn(O,t)}else Dr(n),U()}}}function Dr(n){const e=gt.get(n);e&&(e.flags|=8,gt.delete(n))}Ct().requestIdleCallback;Ct().cancelIdleCallback;const Ge=n=>!!n.type.__asyncLoader,ts=n=>n.type.__isKeepAlive;function $i(n,e){rs(n,"a",e)}function Vi(n,e){rs(n,"da",e)}function rs(n,e,t=un){const r=n.__wdc||(n.__wdc=()=>{let o=t;for(;o;){if(o.isDeactivated)return;o=o.parent}return n()});if(St(e,r,t),t){let o=t.parent;for(;o&&o.parent;)ts(o.parent.vnode)&&qi(r,e,t,o),o=o.parent}}function qi(n,e,t,r){const o=St(e,n,r,!0);ss(()=>{cr(r[e],o)},t)}function St(n,e,t=un,r=!1){if(t){const o=t[n]||(t[n]=[]),s=e.__weh||(e.__weh=(...i)=>{Yn();const a=ot(t),l=$n(e,t,n,i);return a(),Qn(),l});return r?o.unshift(s):o.push(s),s}}const Xn=n=>(e,t=un)=>{(!et||n==="sp")&&St(n,(...r)=>e(...r),t)},Gi=Xn("bm"),os=Xn("m"),Ki=Xn("bu"),Wi=Xn("u"),Ji=Xn("bum"),ss=Xn("um"),Yi=Xn("sp"),Qi=Xn("rtg"),Zi=Xn("rtc");function Xi(n,e=un){St("ec",n,e)}const is="components";function na(n,e){return ta(is,n,!0,e)||n}const ea=Symbol.for("v-ndc");function ta(n,e,t=!0,r=!1){const o=Bn||un;if(o){const s=o.type;if(n===is){const a=Va(s,!1);if(a&&(a===e||a===_n(e)||a===kt(_n(e))))return s}const i=Ur(o[n]||s[n],e)||Ur(o.appContext[n],e);return!i&&r?s:i}}function Ur(n,e){return n&&(n[e]||n[_n(e)]||n[kt(_n(e))])}const Qt=n=>n?Is(n)?Sr(n):Qt(n.parent):null,Ke=on(Object.create(null),{$:n=>n,$el:n=>n.vnode.el,$data:n=>n.data,$props:n=>n.props,$attrs:n=>n.attrs,$slots:n=>n.slots,$refs:n=>n.refs,$parent:n=>Qt(n.parent),$root:n=>Qt(n.root),$host:n=>n.ce,$emit:n=>n.emit,$options:n=>kr(n),$forceUpdate:n=>n.f||(n.f=()=>{wr(n.update)}),$nextTick:n=>n.n||(n.n=Ko.bind(n.proxy)),$watch:n=>ji.bind(n)}),Mt=(n,e)=>n!==Z&&!n.__isScriptSetup&&G(n,e),ra={get({_:n},e){if(e==="__v_skip")return!0;const{ctx:t,setupState:r,data:o,props:s,accessCache:i,type:a,appContext:l}=n;if(e[0]!=="$"){const m=i[e];if(m!==void 0)switch(m){case 1:return r[e];case 2:return o[e];case 4:return t[e];case 3:return s[e]}else{if(Mt(r,e))return i[e]=1,r[e];if(o!==Z&&G(o,e))return i[e]=2,o[e];if(G(s,e))return i[e]=3,s[e];if(t!==Z&&G(t,e))return i[e]=4,t[e];Zt&&(i[e]=0)}}const f=Ke[e];let u,p;if(f)return e==="$attrs"&&dn(n.attrs,"get",""),f(n);if((u=a.__cssModules)&&(u=u[e]))return u;if(t!==Z&&G(t,e))return i[e]=4,t[e];if(p=l.config.globalProperties,G(p,e))return p[e]},set({_:n},e,t){const{data:r,setupState:o,ctx:s}=n;return Mt(o,e)?(o[e]=t,!0):r!==Z&&G(r,e)?(r[e]=t,!0):G(n.props,e)||e[0]==="$"&&e.slice(1)in n?!1:(s[e]=t,!0)},has({_:{data:n,setupState:e,accessCache:t,ctx:r,appContext:o,props:s,type:i}},a){let l;return!!(t[a]||n!==Z&&a[0]!=="$"&&G(n,a)||Mt(e,a)||G(s,a)||G(r,a)||G(Ke,a)||G(o.config.globalProperties,a)||(l=i.__cssModules)&&l[a])},defineProperty(n,e,t){return t.get!=null?n._.accessCache[e]=0:G(t,"value")&&this.set(n,e,t.value,null),Reflect.defineProperty(n,e,t)}};function jr(n){return H(n)?n.reduce((e,t)=>(e[t]=null,e),{}):n}let Zt=!0;function oa(n){const e=kr(n),t=n.proxy,r=n.ctx;Zt=!1,e.beforeCreate&&Hr(e.beforeCreate,n,"bc");const{data:o,computed:s,methods:i,watch:a,provide:l,inject:f,created:u,beforeMount:p,mounted:m,beforeUpdate:b,updated:_,activated:B,deactivated:U,beforeDestroy:O,beforeUnmount:L,destroyed:R,unmounted:P,render:J,renderTracked:ln,renderTriggered:nn,errorCaptured:On,serverPrefetch:ne,expose:Nn,inheritAttrs:ee,components:ue,directives:Rn,filters:Re}=e;if(f&&sa(f,r,null),i)for(const W in i){const $=i[W];j($)&&(r[W]=$.bind(t))}if(o){const W=o.call(t,t);tn(W)&&(n.data=_t(W))}if(Zt=!0,s)for(const W in s){const $=s[W],Vn=j($)?$.bind(t,t):j($.get)?$.get.bind(t,t):Tn,te=!j($)&&j($.set)?$.set.bind(t):Tn,Mn=Sn({get:Vn,set:te});Object.defineProperty(r,W,{enumerable:!0,configurable:!0,get:()=>Mn.value,set:bn=>Mn.value=bn})}if(a)for(const W in a)as(a[W],r,t,W);if(l){const W=j(l)?l.call(t):l;Reflect.ownKeys(W).forEach($=>{lt($,W[$])})}u&&Hr(u,n,"c");function sn(W,$){H($)?$.forEach(Vn=>W(Vn.bind(t))):$&&W($.bind(t))}if(sn(Gi,p),sn(os,m),sn(Ki,b),sn(Wi,_),sn($i,B),sn(Vi,U),sn(Xi,On),sn(Zi,ln),sn(Qi,nn),sn(Ji,L),sn(ss,P),sn(Yi,ne),H(Nn))if(Nn.length){const W=n.exposed||(n.exposed={});Nn.forEach($=>{Object.defineProperty(W,$,{get:()=>t[$],set:Vn=>t[$]=Vn,enumerable:!0})})}else n.exposed||(n.exposed={});J&&n.render===Tn&&(n.render=J),ee!=null&&(n.inheritAttrs=ee),ue&&(n.components=ue),Rn&&(n.directives=Rn),ne&&es(n)}function sa(n,e,t=Tn){H(n)&&(n=Xt(n));for(const r in n){const o=n[r];let s;tn(o)?"default"in o?s=Jn(o.from||r,o.default,!0):s=Jn(o.from||r):s=Jn(o),pn(s)?Object.defineProperty(e,r,{enumerable:!0,configurable:!0,get:()=>s.value,set:i=>s.value=i}):e[r]=s}}function Hr(n,e,t){$n(H(n)?n.map(r=>r.bind(e.proxy)):n.bind(e.proxy),e,t)}function as(n,e,t,r){let o=r.includes(".")?Xo(t,r):()=>t[r];if(an(n)){const s=e[n];j(s)&&ct(o,s)}else if(j(n))ct(o,n.bind(t));else if(tn(n))if(H(n))n.forEach(s=>as(s,e,t,r));else{const s=j(n.handler)?n.handler.bind(t):e[n.handler];j(s)&&ct(o,s,n)}}function kr(n){const e=n.type,{mixins:t,extends:r}=e,{mixins:o,optionsCache:s,config:{optionMergeStrategies:i}}=n.appContext,a=s.get(e);let l;return a?l=a:!o.length&&!t&&!r?l=e:(l={},o.length&&o.forEach(f=>bt(l,f,i,!0)),bt(l,e,i)),tn(e)&&s.set(e,l),l}function bt(n,e,t,r=!1){const{mixins:o,extends:s}=e;s&&bt(n,s,t,!0),o&&o.forEach(i=>bt(n,i,t,!0));for(const i in e)if(!(r&&i==="expose")){const a=ia[i]||t&&t[i];n[i]=a?a(n[i],e[i]):e[i]}return n}const ia={data:Fr,props:zr,emits:zr,methods:He,computed:He,beforeCreate:fn,created:fn,beforeMount:fn,mounted:fn,beforeUpdate:fn,updated:fn,beforeDestroy:fn,beforeUnmount:fn,destroyed:fn,unmounted:fn,activated:fn,deactivated:fn,errorCaptured:fn,serverPrefetch:fn,components:He,directives:He,watch:la,provide:Fr,inject:aa};function Fr(n,e){return e?n?function(){return on(j(n)?n.call(this,this):n,j(e)?e.call(this,this):e)}:e:n}function aa(n,e){return He(Xt(n),Xt(e))}function Xt(n){if(H(n)){const e={};for(let t=0;t<n.length;t++)e[n[t]]=n[t];return e}return n}function fn(n,e){return n?[...new Set([].concat(n,e))]:e}function He(n,e){return n?on(Object.create(null),n,e):e}function zr(n,e){return n?H(n)&&H(e)?[...new Set([...n,...e])]:on(Object.create(null),jr(n),jr(e??{})):e}function la(n,e){if(!n)return e;if(!e)return n;const t=on(Object.create(null),n);for(const r in e)t[r]=fn(n[r],e[r]);return t}function ls(){return{app:null,config:{isNativeTag:Co,performance:!1,globalProperties:{},optionMergeStrategies:{},errorHandler:void 0,warnHandler:void 0,compilerOptions:{}},mixins:[],components:{},directives:{},provides:Object.create(null),optionsCache:new WeakMap,propsCache:new WeakMap,emitsCache:new WeakMap}}let ca=0;function da(n,e){return function(r,o=null){j(r)||(r=on({},r)),o!=null&&!tn(o)&&(o=null);const s=ls(),i=new WeakSet,a=[];let l=!1;const f=s.app={_uid:ca++,_component:r,_props:o,_container:null,_context:s,_instance:null,version:Ga,get config(){return s.config},set config(u){},use(u,...p){return i.has(u)||(u&&j(u.install)?(i.add(u),u.install(f,...p)):j(u)&&(i.add(u),u(f,...p))),f},mixin(u){return s.mixins.includes(u)||s.mixins.push(u),f},component(u,p){return p?(s.components[u]=p,f):s.components[u]},directive(u,p){return p?(s.directives[u]=p,f):s.directives[u]},mount(u,p,m){if(!l){const b=f._ceVNode||Cn(r,o);return b.appContext=s,m===!0?m="svg":m===!1&&(m=void 0),p&&e?e(b,u):n(b,u,m),l=!0,f._container=u,u.__vue_app__=f,Sr(b.component)}},onUnmount(u){a.push(u)},unmount(){l&&($n(a,f._instance,16),n(null,f._container),delete f._container.__vue_app__)},provide(u,p){return s.provides[u]=p,f},runWithContext(u){const p=Te;Te=f;try{return u()}finally{Te=p}}};return f}}let Te=null;const ua=(n,e)=>e==="modelValue"||e==="model-value"?n.modelModifiers:n[`${e}Modifiers`]||n[`${_n(e)}Modifiers`]||n[`${ve(e)}Modifiers`];function pa(n,e,...t){if(n.isUnmounted)return;const r=n.vnode.props||Z;let o=t;const s=e.startsWith("update:"),i=s&&ua(r,e.slice(7));i&&(i.trim&&(o=t.map(u=>an(u)?u.trim():u)),i.number&&(o=t.map(Zs)));let a,l=r[a=Pt(e)]||r[a=Pt(_n(e))];!l&&s&&(l=r[a=Pt(ve(e))]),l&&$n(l,n,6,o);const f=r[a+"Once"];if(f){if(!n.emitted)n.emitted={};else if(n.emitted[a])return;n.emitted[a]=!0,$n(f,n,6,o)}}const fa=new WeakMap;function cs(n,e,t=!1){const r=t?fa:e.emitsCache,o=r.get(n);if(o!==void 0)return o;const s=n.emits;let i={},a=!1;if(!j(n)){const l=f=>{const u=cs(f,e,!0);u&&(a=!0,on(i,u))};!t&&e.mixins.length&&e.mixins.forEach(l),n.extends&&l(n.extends),n.mixins&&n.mixins.forEach(l)}return!s&&!a?(tn(n)&&r.set(n,null),null):(H(s)?s.forEach(l=>i[l]=null):on(i,s),tn(n)&&r.set(n,i),i)}function Bt(n,e){return!n||!xt(e)?!1:(e=e.slice(2).replace(/Once$/,""),G(n,e[0].toLowerCase()+e.slice(1))||G(n,ve(e))||G(n,e))}function Dt(n){const{type:e,vnode:t,proxy:r,withProxy:o,propsOptions:[s],slots:i,attrs:a,emit:l,render:f,renderCache:u,props:p,data:m,setupState:b,ctx:_,inheritAttrs:B}=n,U=mt(n);let O,L;try{if(t.shapeFlag&4){const P=o||r,J=P;O=zn(f.call(J,P,u,p,b,m,_)),L=a}else{const P=e;O=zn(P.length>1?P(p,{attrs:a,slots:i,emit:l}):P(p,null)),L=e.props?a:ma(a)}}catch(P){We.length=0,It(P,n,1),O=Cn(Le)}let R=O;if(L&&B!==!1){const P=Object.keys(L),{shapeFlag:J}=R;P.length&&J&7&&(s&&P.some(lr)&&(L=ga(L,s)),R=Pe(R,L,!1,!0))}return t.dirs&&(R=Pe(R,null,!1,!0),R.dirs=R.dirs?R.dirs.concat(t.dirs):t.dirs),t.transition&&Er(R,t.transition),O=R,mt(U),O}const ma=n=>{let e;for(const t in n)(t==="class"||t==="style"||xt(t))&&((e||(e={}))[t]=n[t]);return e},ga=(n,e)=>{const t={};for(const r in n)(!lr(r)||!(r.slice(9)in e))&&(t[r]=n[r]);return t};function ba(n,e,t){const{props:r,children:o,component:s}=n,{props:i,children:a,patchFlag:l}=e,f=s.emitsOptions;if(e.dirs||e.transition)return!0;if(t&&l>=0){if(l&1024)return!0;if(l&16)return r?$r(r,i,f):!!i;if(l&8){const u=e.dynamicProps;for(let p=0;p<u.length;p++){const m=u[p];if(i[m]!==r[m]&&!Bt(f,m))return!0}}}else return(o||a)&&(!a||!a.$stable)?!0:r===i?!1:r?i?$r(r,i,f):!0:!!i;return!1}function $r(n,e,t){const r=Object.keys(e);if(r.length!==Object.keys(n).length)return!0;for(let o=0;o<r.length;o++){const s=r[o];if(e[s]!==n[s]&&!Bt(t,s))return!0}return!1}function ha({vnode:n,parent:e},t){for(;e;){const r=e.subTree;if(r.suspense&&r.suspense.activeBranch===n&&(r.el=n.el),r===n)(n=e.vnode).el=t,e=e.parent;else break}}const ds={},us=()=>Object.create(ds),ps=n=>Object.getPrototypeOf(n)===ds;function va(n,e,t,r=!1){const o={},s=us();n.propsDefaults=Object.create(null),fs(n,e,o,s);for(const i in n.propsOptions[0])i in o||(o[i]=void 0);t?n.props=r?o:zo(o):n.type.props?n.props=o:n.props=s,n.attrs=s}function ya(n,e,t,r){const{props:o,attrs:s,vnode:{patchFlag:i}}=n,a=q(o),[l]=n.propsOptions;let f=!1;if((r||i>0)&&!(i&16)){if(i&8){const u=n.vnode.dynamicProps;for(let p=0;p<u.length;p++){let m=u[p];if(Bt(n.emitsOptions,m))continue;const b=e[m];if(l)if(G(s,m))b!==s[m]&&(s[m]=b,f=!0);else{const _=_n(m);o[_]=nr(l,a,_,b,n,!1)}else b!==s[m]&&(s[m]=b,f=!0)}}}else{fs(n,e,o,s)&&(f=!0);let u;for(const p in a)(!e||!G(e,p)&&((u=ve(p))===p||!G(e,u)))&&(l?t&&(t[p]!==void 0||t[u]!==void 0)&&(o[p]=nr(l,a,p,void 0,n,!0)):delete o[p]);if(s!==a)for(const p in s)(!e||!G(e,p))&&(delete s[p],f=!0)}f&&Wn(n.attrs,"set","")}function fs(n,e,t,r){const[o,s]=n.propsOptions;let i=!1,a;if(e)for(let l in e){if(ze(l))continue;const f=e[l];let u;o&&G(o,u=_n(l))?!s||!s.includes(u)?t[u]=f:(a||(a={}))[u]=f:Bt(n.emitsOptions,l)||(!(l in r)||f!==r[l])&&(r[l]=f,i=!0)}if(s){const l=q(t),f=a||Z;for(let u=0;u<s.length;u++){const p=s[u];t[p]=nr(o,l,p,f[p],n,!G(f,p))}}return i}function nr(n,e,t,r,o,s){const i=n[t];if(i!=null){const a=G(i,"default");if(a&&r===void 0){const l=i.default;if(i.type!==Function&&!i.skipFactory&&j(l)){const{propsDefaults:f}=o;if(t in f)r=f[t];else{const u=ot(o);r=f[t]=l.call(null,e),u()}}else r=l;o.ce&&o.ce._setProp(t,r)}i[0]&&(s&&!a?r=!1:i[1]&&(r===""||r===ve(t))&&(r=!0))}return r}const xa=new WeakMap;function ms(n,e,t=!1){const r=t?xa:e.propsCache,o=r.get(n);if(o)return o;const s=n.props,i={},a=[];let l=!1;if(!j(n)){const u=p=>{l=!0;const[m,b]=ms(p,e,!0);on(i,m),b&&a.push(...b)};!t&&e.mixins.length&&e.mixins.forEach(u),n.extends&&u(n.extends),n.mixins&&n.mixins.forEach(u)}if(!s&&!l)return tn(n)&&r.set(n,_e),_e;if(H(s))for(let u=0;u<s.length;u++){const p=_n(s[u]);Vr(p)&&(i[p]=Z)}else if(s)for(const u in s){const p=_n(u);if(Vr(p)){const m=s[u],b=i[p]=H(m)||j(m)?{type:m}:on({},m),_=b.type;let B=!1,U=!0;if(H(_))for(let O=0;O<_.length;++O){const L=_[O],R=j(L)&&L.name;if(R==="Boolean"){B=!0;break}else R==="String"&&(U=!1)}else B=j(_)&&_.name==="Boolean";b[0]=B,b[1]=U,(B||G(b,"default"))&&a.push(p)}}const f=[i,a];return tn(n)&&r.set(n,f),f}function Vr(n){return n[0]!=="$"&&!ze(n)}const Cr=n=>n==="_"||n==="_ctx"||n==="$stable",_r=n=>H(n)?n.map(zn):[zn(n)],wa=(n,e,t)=>{if(e._n)return e;const r=Mi((...o)=>_r(e(...o)),t);return r._c=!1,r},gs=(n,e,t)=>{const r=n._ctx;for(const o in n){if(Cr(o))continue;const s=n[o];if(j(s))e[o]=wa(o,s,r);else if(s!=null){const i=_r(s);e[o]=()=>i}}},bs=(n,e)=>{const t=_r(e);n.slots.default=()=>t},hs=(n,e,t)=>{for(const r in e)(t||!Cr(r))&&(n[r]=e[r])},Ea=(n,e,t)=>{const r=n.slots=us();if(n.vnode.shapeFlag&32){const o=e._;o?(hs(r,e,t),t&&Io(r,"_",o,!0)):gs(e,r)}else e&&bs(n,e)},ka=(n,e,t)=>{const{vnode:r,slots:o}=n;let s=!0,i=Z;if(r.shapeFlag&32){const a=e._;a?t&&a===1?s=!1:hs(o,e,t):(s=!e.$stable,gs(e,o)),i=e}else e&&(bs(n,e),i={default:1});if(s)for(const a in o)!Cr(a)&&i[a]==null&&delete o[a]},wn=Ba;function Ca(n){return _a(n)}function _a(n,e){const t=Ct();t.__VUE__=!0;const{insert:r,remove:o,patchProp:s,createElement:i,createText:a,createComment:l,setText:f,setElementText:u,parentNode:p,nextSibling:m,setScopeId:b=Tn,insertStaticContent:_}=n,B=(c,d,g,y=null,h=null,x=null,C=void 0,k=null,E=!!d.dynamicChildren)=>{if(c===d)return;c&&!Ue(c,d)&&(y=v(c),bn(c,h,x,!0),c=null),d.patchFlag===-2&&(E=!1,d.dynamicChildren=null);const{type:w,ref:M,shapeFlag:S}=d;switch(w){case Tt:U(c,d,g,y);break;case Le:O(c,d,g,y);break;case jt:c==null&&L(d,g,y,C);break;case Fn:ue(c,d,g,y,h,x,C,k,E);break;default:S&1?J(c,d,g,y,h,x,C,k,E):S&6?Rn(c,d,g,y,h,x,C,k,E):(S&64||S&128)&&w.process(c,d,g,y,h,x,C,k,E,N)}M!=null&&h?qe(M,c&&c.ref,x,d||c,!d):M==null&&c&&c.ref!=null&&qe(c.ref,null,x,c,!0)},U=(c,d,g,y)=>{if(c==null)r(d.el=a(d.children),g,y);else{const h=d.el=c.el;d.children!==c.children&&f(h,d.children)}},O=(c,d,g,y)=>{c==null?r(d.el=l(d.children||""),g,y):d.el=c.el},L=(c,d,g,y)=>{[c.el,c.anchor]=_(c.children,d,g,y,c.el,c.anchor)},R=({el:c,anchor:d},g,y)=>{let h;for(;c&&c!==d;)h=m(c),r(c,g,y),c=h;r(d,g,y)},P=({el:c,anchor:d})=>{let g;for(;c&&c!==d;)g=m(c),o(c),c=g;o(d)},J=(c,d,g,y,h,x,C,k,E)=>{if(d.type==="svg"?C="svg":d.type==="math"&&(C="mathml"),c==null)ln(d,g,y,h,x,C,k,E);else{const w=c.el&&c.el._isVueCE?c.el:null;try{w&&w._beginPatch(),ne(c,d,h,x,C,k,E)}finally{w&&w._endPatch()}}},ln=(c,d,g,y,h,x,C,k)=>{let E,w;const{props:M,shapeFlag:S,transition:A,dirs:D}=c;if(E=c.el=i(c.type,x,M&&M.is,M),S&8?u(E,c.children):S&16&&On(c.children,E,null,y,h,Ut(c,x),C,k),D&&fe(c,null,y,"created"),nn(E,c,c.scopeId,C,y),M){for(const Y in M)Y!=="value"&&!ze(Y)&&s(E,Y,null,M[Y],x,y);"value"in M&&s(E,"value",null,M.value,x),(w=M.onVnodeBeforeMount)&&Un(w,y,c)}D&&fe(c,null,y,"beforeMount");const z=Ia(h,A);z&&A.beforeEnter(E),r(E,d,g),((w=M&&M.onVnodeMounted)||z||D)&&wn(()=>{w&&Un(w,y,c),z&&A.enter(E),D&&fe(c,null,y,"mounted")},h)},nn=(c,d,g,y,h)=>{if(g&&b(c,g),y)for(let x=0;x<y.length;x++)b(c,y[x]);if(h){let x=h.subTree;if(d===x||ws(x.type)&&(x.ssContent===d||x.ssFallback===d)){const C=h.vnode;nn(c,C,C.scopeId,C.slotScopeIds,h.parent)}}},On=(c,d,g,y,h,x,C,k,E=0)=>{for(let w=E;w<c.length;w++){const M=c[w]=k?ie(c[w]):zn(c[w]);B(null,M,d,g,y,h,x,C,k)}},ne=(c,d,g,y,h,x,C)=>{const k=d.el=c.el;let{patchFlag:E,dynamicChildren:w,dirs:M}=d;E|=c.patchFlag&16;const S=c.props||Z,A=d.props||Z;let D;if(g&&me(g,!1),(D=A.onVnodeBeforeUpdate)&&Un(D,g,d,c),M&&fe(d,c,g,"beforeUpdate"),g&&me(g,!0),(S.innerHTML&&A.innerHTML==null||S.textContent&&A.textContent==null)&&u(k,""),w?Nn(c.dynamicChildren,w,k,g,y,Ut(d,h),x):C||$(c,d,k,null,g,y,Ut(d,h),x,!1),E>0){if(E&16)ee(k,S,A,g,h);else if(E&2&&S.class!==A.class&&s(k,"class",null,A.class,h),E&4&&s(k,"style",S.style,A.style,h),E&8){const z=d.dynamicProps;for(let Y=0;Y<z.length;Y++){const K=z[Y],hn=S[K],cn=A[K];(cn!==hn||K==="value")&&s(k,K,hn,cn,h,g)}}E&1&&c.children!==d.children&&u(k,d.children)}else!C&&w==null&&ee(k,S,A,g,h);((D=A.onVnodeUpdated)||M)&&wn(()=>{D&&Un(D,g,d,c),M&&fe(d,c,g,"updated")},y)},Nn=(c,d,g,y,h,x,C)=>{for(let k=0;k<d.length;k++){const E=c[k],w=d[k],M=E.el&&(E.type===Fn||!Ue(E,w)||E.shapeFlag&198)?p(E.el):g;B(E,w,M,null,y,h,x,C,!0)}},ee=(c,d,g,y,h)=>{if(d!==g){if(d!==Z)for(const x in d)!ze(x)&&!(x in g)&&s(c,x,d[x],null,h,y);for(const x in g){if(ze(x))continue;const C=g[x],k=d[x];C!==k&&x!=="value"&&s(c,x,k,C,h,y)}"value"in g&&s(c,"value",d.value,g.value,h)}},ue=(c,d,g,y,h,x,C,k,E)=>{const w=d.el=c?c.el:a(""),M=d.anchor=c?c.anchor:a("");let{patchFlag:S,dynamicChildren:A,slotScopeIds:D}=d;D&&(k=k?k.concat(D):D),c==null?(r(w,g,y),r(M,g,y),On(d.children||[],g,M,h,x,C,k,E)):S>0&&S&64&&A&&c.dynamicChildren&&c.dynamicChildren.length===A.length?(Nn(c.dynamicChildren,A,g,h,x,C,k),(d.key!=null||h&&d===h.subTree)&&vs(c,d,!0)):$(c,d,g,M,h,x,C,k,E)},Rn=(c,d,g,y,h,x,C,k,E)=>{d.slotScopeIds=k,c==null?d.shapeFlag&512?h.ctx.activate(d,g,y,C,E):Re(d,g,y,h,x,C,E):ye(c,d,E)},Re=(c,d,g,y,h,x,C)=>{const k=c.component=Ua(c,y,h);if(ts(c)&&(k.ctx.renderer=N),Ha(k,!1,C),k.asyncDep){if(h&&h.registerDep(k,sn,C),!c.el){const E=k.subTree=Cn(Le);O(null,E,d,g),c.placeholder=E.el}}else sn(k,c,d,g,h,x,C)},ye=(c,d,g)=>{const y=d.component=c.component;if(ba(c,d,g))if(y.asyncDep&&!y.asyncResolved){W(y,d,g);return}else y.next=d,y.update();else d.el=c.el,y.vnode=d},sn=(c,d,g,y,h,x,C)=>{const k=()=>{if(c.isMounted){let{next:S,bu:A,u:D,parent:z,vnode:Y}=c;{const yn=ys(c);if(yn){S&&(S.el=Y.el,W(c,S,C)),yn.asyncDep.then(()=>{c.isUnmounted||k()});return}}let K=S,hn;me(c,!1),S?(S.el=Y.el,W(c,S,C)):S=Y,A&&At(A),(hn=S.props&&S.props.onVnodeBeforeUpdate)&&Un(hn,z,S,Y),me(c,!0);const cn=Dt(c),In=c.subTree;c.subTree=cn,B(In,cn,p(In.el),v(In),c,h,x),S.el=cn.el,K===null&&ha(c,cn.el),D&&wn(D,h),(hn=S.props&&S.props.onVnodeUpdated)&&wn(()=>Un(hn,z,S,Y),h)}else{let S;const{el:A,props:D}=d,{bm:z,m:Y,parent:K,root:hn,type:cn}=c,In=Ge(d);if(me(c,!1),z&&At(z),!In&&(S=D&&D.onVnodeBeforeMount)&&Un(S,K,d),me(c,!0),A&&X){const yn=()=>{c.subTree=Dt(c),X(A,c.subTree,c,h,null)};In&&cn.__asyncHydrate?cn.__asyncHydrate(A,c,yn):yn()}else{hn.ce&&hn.ce._def.shadowRoot!==!1&&hn.ce._injectChildStyle(cn);const yn=c.subTree=Dt(c);B(null,yn,g,y,c,h,x),d.el=yn.el}if(Y&&wn(Y,h),!In&&(S=D&&D.onVnodeMounted)){const yn=d;wn(()=>Un(S,K,yn),h)}(d.shapeFlag&256||K&&Ge(K.vnode)&&K.vnode.shapeFlag&256)&&c.a&&wn(c.a,h),c.isMounted=!0,d=g=y=null}};c.scope.on();const E=c.effect=new Bo(k);c.scope.off();const w=c.update=E.run.bind(E),M=c.job=E.runIfDirty.bind(E);M.i=c,M.id=c.uid,E.scheduler=()=>wr(M),me(c,!0),w()},W=(c,d,g)=>{d.component=c;const y=c.vnode.props;c.vnode=d,c.next=null,ya(c,d.props,y,g),ka(c,d.children,g),Yn(),Mr(c),Qn()},$=(c,d,g,y,h,x,C,k,E=!1)=>{const w=c&&c.children,M=c?c.shapeFlag:0,S=d.children,{patchFlag:A,shapeFlag:D}=d;if(A>0){if(A&128){te(w,S,g,y,h,x,C,k,E);return}else if(A&256){Vn(w,S,g,y,h,x,C,k,E);return}}D&8?(M&16&&kn(w,h,x),S!==w&&u(g,S)):M&16?D&16?te(w,S,g,y,h,x,C,k,E):kn(w,h,x,!0):(M&8&&u(g,""),D&16&&On(S,g,y,h,x,C,k,E))},Vn=(c,d,g,y,h,x,C,k,E)=>{c=c||_e,d=d||_e;const w=c.length,M=d.length,S=Math.min(w,M);let A;for(A=0;A<S;A++){const D=d[A]=E?ie(d[A]):zn(d[A]);B(c[A],D,g,null,h,x,C,k,E)}w>M?kn(c,h,x,!0,!1,S):On(d,g,y,h,x,C,k,E,S)},te=(c,d,g,y,h,x,C,k,E)=>{let w=0;const M=d.length;let S=c.length-1,A=M-1;for(;w<=S&&w<=A;){const D=c[w],z=d[w]=E?ie(d[w]):zn(d[w]);if(Ue(D,z))B(D,z,g,null,h,x,C,k,E);else break;w++}for(;w<=S&&w<=A;){const D=c[S],z=d[A]=E?ie(d[A]):zn(d[A]);if(Ue(D,z))B(D,z,g,null,h,x,C,k,E);else break;S--,A--}if(w>S){if(w<=A){const D=A+1,z=D<M?d[D].el:y;for(;w<=A;)B(null,d[w]=E?ie(d[w]):zn(d[w]),g,z,h,x,C,k,E),w++}}else if(w>A)for(;w<=S;)bn(c[w],h,x,!0),w++;else{const D=w,z=w,Y=new Map;for(w=z;w<=A;w++){const xn=d[w]=E?ie(d[w]):zn(d[w]);xn.key!=null&&Y.set(xn.key,w)}let K,hn=0;const cn=A-z+1;let In=!1,yn=0;const Me=new Array(cn);for(w=0;w<cn;w++)Me[w]=0;for(w=D;w<=S;w++){const xn=c[w];if(hn>=cn){bn(xn,h,x,!0);continue}let Dn;if(xn.key!=null)Dn=Y.get(xn.key);else for(K=z;K<=A;K++)if(Me[K-z]===0&&Ue(xn,d[K])){Dn=K;break}Dn===void 0?bn(xn,h,x,!0):(Me[Dn-z]=w+1,Dn>=yn?yn=Dn:In=!0,B(xn,d[Dn],g,null,h,x,C,k,E),hn++)}const Lr=In?Sa(Me):_e;for(K=Lr.length-1,w=cn-1;w>=0;w--){const xn=z+w,Dn=d[xn],Pr=d[xn+1],Ar=xn+1<M?Pr.el||xs(Pr):y;Me[w]===0?B(null,Dn,g,Ar,h,x,C,k,E):In&&(K<0||w!==Lr[K]?Mn(Dn,g,Ar,2):K--)}}},Mn=(c,d,g,y,h=null)=>{const{el:x,type:C,transition:k,children:E,shapeFlag:w}=c;if(w&6){Mn(c.component.subTree,d,g,y);return}if(w&128){c.suspense.move(d,g,y);return}if(w&64){C.move(c,d,g,N);return}if(C===Fn){r(x,d,g);for(let S=0;S<E.length;S++)Mn(E[S],d,g,y);r(c.anchor,d,g);return}if(C===jt){R(c,d,g);return}if(y!==2&&w&1&&k)if(y===0)k.beforeEnter(x),r(x,d,g),wn(()=>k.enter(x),h);else{const{leave:S,delayLeave:A,afterLeave:D}=k,z=()=>{c.ctx.isUnmounted?o(x):r(x,d,g)},Y=()=>{x._isLeaving&&x[zi](!0),S(x,()=>{z(),D&&D()})};A?A(x,z,Y):Y()}else r(x,d,g)},bn=(c,d,g,y=!1,h=!1)=>{const{type:x,props:C,ref:k,children:E,dynamicChildren:w,shapeFlag:M,patchFlag:S,dirs:A,cacheIndex:D}=c;if(S===-2&&(h=!1),k!=null&&(Yn(),qe(k,null,g,c,!0),Qn()),D!=null&&(d.renderCache[D]=void 0),M&256){d.ctx.deactivate(c);return}const z=M&1&&A,Y=!Ge(c);let K;if(Y&&(K=C&&C.onVnodeBeforeUnmount)&&Un(K,d,c),M&6)pe(c.component,g,y);else{if(M&128){c.suspense.unmount(g,y);return}z&&fe(c,null,d,"beforeUnmount"),M&64?c.type.remove(c,d,g,N,y):w&&!w.hasOnce&&(x!==Fn||S>0&&S&64)?kn(w,d,g,!1,!0):(x===Fn&&S&384||!h&&M&16)&&kn(E,d,g),y&&xe(c)}(Y&&(K=C&&C.onVnodeUnmounted)||z)&&wn(()=>{K&&Un(K,d,c),z&&fe(c,null,d,"unmounted")},g)},xe=c=>{const{type:d,el:g,anchor:y,transition:h}=c;if(d===Fn){we(g,y);return}if(d===jt){P(c);return}const x=()=>{o(g),h&&!h.persisted&&h.afterLeave&&h.afterLeave()};if(c.shapeFlag&1&&h&&!h.persisted){const{leave:C,delayLeave:k}=h,E=()=>C(g,x);k?k(c.el,x,E):E()}else x()},we=(c,d)=>{let g;for(;c!==d;)g=m(c),o(c),c=g;o(d)},pe=(c,d,g)=>{const{bum:y,scope:h,job:x,subTree:C,um:k,m:E,a:w}=c;qr(E),qr(w),y&&At(y),h.stop(),x&&(x.flags|=8,bn(C,c,d,g)),k&&wn(k,d),wn(()=>{c.isUnmounted=!0},d)},kn=(c,d,g,y=!1,h=!1,x=0)=>{for(let C=x;C<c.length;C++)bn(c[C],d,g,y,h)},v=c=>{if(c.shapeFlag&6)return v(c.component.subTree);if(c.shapeFlag&128)return c.suspense.next();const d=m(c.anchor||c.el),g=d&&d[Hi];return g?m(g):d};let T=!1;const I=(c,d,g)=>{let y;c==null?d._vnode&&(bn(d._vnode,null,null,!0),y=d._vnode.component):B(d._vnode||null,c,d,null,null,null,g),d._vnode=c,T||(T=!0,Mr(y),Jo(),T=!1)},N={p:B,um:bn,m:Mn,r:xe,mt:Re,mc:On,pc:$,pbc:Nn,n:v,o:n};let F,X;return e&&([F,X]=e(N)),{render:I,hydrate:F,createApp:da(I,F)}}function Ut({type:n,props:e},t){return t==="svg"&&n==="foreignObject"||t==="mathml"&&n==="annotation-xml"&&e&&e.encoding&&e.encoding.includes("html")?void 0:t}function me({effect:n,job:e},t){t?(n.flags|=32,e.flags|=4):(n.flags&=-33,e.flags&=-5)}function Ia(n,e){return(!n||n&&!n.pendingBranch)&&e&&!e.persisted}function vs(n,e,t=!1){const r=n.children,o=e.children;if(H(r)&&H(o))for(let s=0;s<r.length;s++){const i=r[s];let a=o[s];a.shapeFlag&1&&!a.dynamicChildren&&((a.patchFlag<=0||a.patchFlag===32)&&(a=o[s]=ie(o[s]),a.el=i.el),!t&&a.patchFlag!==-2&&vs(i,a)),a.type===Tt&&(a.patchFlag!==-1?a.el=i.el:a.__elIndex=s+(n.type===Fn?1:0)),a.type===Le&&!a.el&&(a.el=i.el)}}function Sa(n){const e=n.slice(),t=[0];let r,o,s,i,a;const l=n.length;for(r=0;r<l;r++){const f=n[r];if(f!==0){if(o=t[t.length-1],n[o]<f){e[r]=o,t.push(r);continue}for(s=0,i=t.length-1;s<i;)a=s+i>>1,n[t[a]]<f?s=a+1:i=a;f<n[t[s]]&&(s>0&&(e[r]=t[s-1]),t[s]=r)}}for(s=t.length,i=t[s-1];s-- >0;)t[s]=i,i=e[i];return t}function ys(n){const e=n.subTree.component;if(e)return e.asyncDep&&!e.asyncResolved?e:ys(e)}function qr(n){if(n)for(let e=0;e<n.length;e++)n[e].flags|=8}function xs(n){if(n.placeholder)return n.placeholder;const e=n.component;return e?xs(e.subTree):null}const ws=n=>n.__isSuspense;function Ba(n,e){e&&e.pendingBranch?H(n)?e.effects.push(...n):e.effects.push(n):Ri(n)}const Fn=Symbol.for("v-fgt"),Tt=Symbol.for("v-txt"),Le=Symbol.for("v-cmt"),jt=Symbol.for("v-stc"),We=[];let En=null;function Es(n=!1){We.push(En=n?null:[])}function Ta(){We.pop(),En=We[We.length-1]||null}let nt=1;function ht(n,e=!1){nt+=n,n<0&&En&&e&&(En.hasOnce=!0)}function ks(n){return n.dynamicChildren=nt>0?En||_e:null,Ta(),nt>0&&En&&En.push(n),n}function La(n,e,t,r,o,s){return ks(_s(n,e,t,r,o,s,!0))}function Pa(n,e,t,r,o){return ks(Cn(n,e,t,r,o,!0))}function vt(n){return n?n.__v_isVNode===!0:!1}function Ue(n,e){return n.type===e.type&&n.key===e.key}const Cs=({key:n})=>n??null,dt=({ref:n,ref_key:e,ref_for:t})=>(typeof n=="number"&&(n=""+n),n!=null?an(n)||pn(n)||j(n)?{i:Bn,r:n,k:e,f:!!t}:n:null);function _s(n,e=null,t=null,r=0,o=null,s=n===Fn?0:1,i=!1,a=!1){const l={__v_isVNode:!0,__v_skip:!0,type:n,props:e,key:e&&Cs(e),ref:e&&dt(e),scopeId:Qo,slotScopeIds:null,children:t,component:null,suspense:null,ssContent:null,ssFallback:null,dirs:null,transition:null,el:null,anchor:null,target:null,targetStart:null,targetAnchor:null,staticCount:0,shapeFlag:s,patchFlag:r,dynamicProps:o,dynamicChildren:null,appContext:null,ctx:Bn};return a?(Ir(l,t),s&128&&n.normalize(l)):t&&(l.shapeFlag|=an(t)?8:16),nt>0&&!i&&En&&(l.patchFlag>0||s&6)&&l.patchFlag!==32&&En.push(l),l}const Cn=Aa;function Aa(n,e=null,t=null,r=0,o=null,s=!1){if((!n||n===ea)&&(n=Le),vt(n)){const a=Pe(n,e,!0);return t&&Ir(a,t),nt>0&&!s&&En&&(a.shapeFlag&6?En[En.indexOf(n)]=a:En.push(a)),a.patchFlag=-2,a}if(qa(n)&&(n=n.__vccOpts),e){e=Oa(e);let{class:a,style:l}=e;a&&!an(a)&&(e.class=pr(a)),tn(l)&&(xr(l)&&!H(l)&&(l=on({},l)),e.style=ur(l))}const i=an(n)?1:ws(n)?128:Fi(n)?64:tn(n)?4:j(n)?2:0;return _s(n,e,t,r,o,i,s,!0)}function Oa(n){return n?xr(n)||ps(n)?on({},n):n:null}function Pe(n,e,t=!1,r=!1){const{props:o,ref:s,patchFlag:i,children:a,transition:l}=n,f=e?Ra(o||{},e):o,u={__v_isVNode:!0,__v_skip:!0,type:n.type,props:f,key:f&&Cs(f),ref:e&&e.ref?t&&s?H(s)?s.concat(dt(e)):[s,dt(e)]:dt(e):s,scopeId:n.scopeId,slotScopeIds:n.slotScopeIds,children:a,target:n.target,targetStart:n.targetStart,targetAnchor:n.targetAnchor,staticCount:n.staticCount,shapeFlag:n.shapeFlag,patchFlag:e&&n.type!==Fn?i===-1?16:i|16:i,dynamicProps:n.dynamicProps,dynamicChildren:n.dynamicChildren,appContext:n.appContext,dirs:n.dirs,transition:l,component:n.component,suspense:n.suspense,ssContent:n.ssContent&&Pe(n.ssContent),ssFallback:n.ssFallback&&Pe(n.ssFallback),placeholder:n.placeholder,el:n.el,anchor:n.anchor,ctx:n.ctx,ce:n.ce};return l&&r&&Er(u,l.clone(u)),u}function Na(n=" ",e=0){return Cn(Tt,null,n,e)}function zn(n){return n==null||typeof n=="boolean"?Cn(Le):H(n)?Cn(Fn,null,n.slice()):vt(n)?ie(n):Cn(Tt,null,String(n))}function ie(n){return n.el===null&&n.patchFlag!==-1||n.memo?n:Pe(n)}function Ir(n,e){let t=0;const{shapeFlag:r}=n;if(e==null)e=null;else if(H(e))t=16;else if(typeof e=="object")if(r&65){const o=e.default;o&&(o._c&&(o._d=!1),Ir(n,o()),o._c&&(o._d=!0));return}else{t=32;const o=e._;!o&&!ps(e)?e._ctx=Bn:o===3&&Bn&&(Bn.slots._===1?e._=1:(e._=2,n.patchFlag|=1024))}else j(e)?(e={default:e,_ctx:Bn},t=32):(e=String(e),r&64?(t=16,e=[Na(e)]):t=8);n.children=e,n.shapeFlag|=t}function Ra(...n){const e={};for(let t=0;t<n.length;t++){const r=n[t];for(const o in r)if(o==="class")e.class!==r.class&&(e.class=pr([e.class,r.class]));else if(o==="style")e.style=ur([e.style,r.style]);else if(xt(o)){const s=e[o],i=r[o];i&&s!==i&&!(H(s)&&s.includes(i))&&(e[o]=s?[].concat(s,i):i)}else o!==""&&(e[o]=r[o])}return e}function Un(n,e,t,r=null){$n(n,e,7,[t,r])}const Ma=ls();let Da=0;function Ua(n,e,t){const r=n.type,o=(e?e.appContext:n.appContext)||Ma,s={uid:Da++,vnode:n,type:r,parent:e,appContext:o,root:null,next:null,subTree:null,effect:null,update:null,job:null,scope:new si(!0),render:null,proxy:null,exposed:null,exposeProxy:null,withProxy:null,provides:e?e.provides:Object.create(o.provides),ids:e?e.ids:["",0,0],accessCache:null,renderCache:[],components:null,directives:null,propsOptions:ms(r,o),emitsOptions:cs(r,o),emit:null,emitted:null,propsDefaults:Z,inheritAttrs:r.inheritAttrs,ctx:Z,data:Z,props:Z,attrs:Z,slots:Z,refs:Z,setupState:Z,setupContext:null,suspense:t,suspenseId:t?t.pendingId:0,asyncDep:null,asyncResolved:!1,isMounted:!1,isUnmounted:!1,isDeactivated:!1,bc:null,c:null,bm:null,m:null,bu:null,u:null,um:null,bum:null,da:null,a:null,rtg:null,rtc:null,ec:null,sp:null};return s.ctx={_:s},s.root=e?e.root:s,s.emit=pa.bind(null,s),n.ce&&n.ce(s),s}let un=null;const ja=()=>un||Bn;let yt,er;{const n=Ct(),e=(t,r)=>{let o;return(o=n[t])||(o=n[t]=[]),o.push(r),s=>{o.length>1?o.forEach(i=>i(s)):o[0](s)}};yt=e("__VUE_INSTANCE_SETTERS__",t=>un=t),er=e("__VUE_SSR_SETTERS__",t=>et=t)}const ot=n=>{const e=un;return yt(n),n.scope.on(),()=>{n.scope.off(),yt(e)}},Gr=()=>{un&&un.scope.off(),yt(null)};function Is(n){return n.vnode.shapeFlag&4}let et=!1;function Ha(n,e=!1,t=!1){e&&er(e);const{props:r,children:o}=n.vnode,s=Is(n);va(n,r,s,e),Ea(n,o,t||e);const i=s?Fa(n,e):void 0;return e&&er(!1),i}function Fa(n,e){const t=n.type;n.accessCache=Object.create(null),n.proxy=new Proxy(n.ctx,ra);const{setup:r}=t;if(r){Yn();const o=n.setupContext=r.length>1?$a(n):null,s=ot(n),i=rt(r,n,0,[n.props,o]),a=_o(i);if(Qn(),s(),(a||n.sp)&&!Ge(n)&&es(n),a){if(i.then(Gr,Gr),e)return i.then(l=>{Kr(n,l,e)}).catch(l=>{It(l,n,0)});n.asyncDep=i}else Kr(n,i,e)}else Ss(n,e)}function Kr(n,e,t){j(e)?n.type.__ssrInlineRender?n.ssrRender=e:n.render=e:tn(e)&&(n.setupState=qo(e)),Ss(n,t)}let Wr;function Ss(n,e,t){const r=n.type;if(!n.render){if(!e&&Wr&&!r.render){const o=r.template||kr(n).template;if(o){const{isCustomElement:s,compilerOptions:i}=n.appContext.config,{delimiters:a,compilerOptions:l}=r,f=on(on({isCustomElement:s,delimiters:a},i),l);r.render=Wr(o,f)}}n.render=r.render||Tn}{const o=ot(n);Yn();try{oa(n)}finally{Qn(),o()}}}const za={get(n,e){return dn(n,"get",""),n[e]}};function $a(n){const e=t=>{n.exposed=t||{}};return{attrs:new Proxy(n.attrs,za),slots:n.slots,emit:n.emit,expose:e}}function Sr(n){return n.exposed?n.exposeProxy||(n.exposeProxy=new Proxy(qo(_i(n.exposed)),{get(e,t){if(t in e)return e[t];if(t in Ke)return Ke[t](n)},has(e,t){return t in e||t in Ke}})):n.proxy}function Va(n,e=!0){return j(n)?n.displayName||n.name:n.name||e&&n.__name}function qa(n){return j(n)&&"__vccOpts"in n}const Sn=(n,e)=>Li(n,e,et);function Bs(n,e,t){try{ht(-1);const r=arguments.length;return r===2?tn(e)&&!H(e)?vt(e)?Cn(n,null,[e]):Cn(n,e):Cn(n,null,e):(r>3?t=Array.prototype.slice.call(arguments,2):r===3&&vt(t)&&(t=[t]),Cn(n,e,t))}finally{ht(1)}}const Ga="3.5.27";/**
* @vue/runtime-dom v3.5.27
* (c) 2018-present Yuxi (Evan) You and Vue contributors
* @license MIT
**/let tr;const Jr=typeof window<"u"&&window.trustedTypes;if(Jr)try{tr=Jr.createPolicy("vue",{createHTML:n=>n})}catch{}const Ts=tr?n=>tr.createHTML(n):n=>n,Ka="http://www.w3.org/2000/svg",Wa="http://www.w3.org/1998/Math/MathML",Kn=typeof document<"u"?document:null,Yr=Kn&&Kn.createElement("template"),Ja={insert:(n,e,t)=>{e.insertBefore(n,t||null)},remove:n=>{const e=n.parentNode;e&&e.removeChild(n)},createElement:(n,e,t,r)=>{const o=e==="svg"?Kn.createElementNS(Ka,n):e==="mathml"?Kn.createElementNS(Wa,n):t?Kn.createElement(n,{is:t}):Kn.createElement(n);return n==="select"&&r&&r.multiple!=null&&o.setAttribute("multiple",r.multiple),o},createText:n=>Kn.createTextNode(n),createComment:n=>Kn.createComment(n),setText:(n,e)=>{n.nodeValue=e},setElementText:(n,e)=>{n.textContent=e},parentNode:n=>n.parentNode,nextSibling:n=>n.nextSibling,querySelector:n=>Kn.querySelector(n),setScopeId(n,e){n.setAttribute(e,"")},insertStaticContent(n,e,t,r,o,s){const i=t?t.previousSibling:e.lastChild;if(o&&(o===s||o.nextSibling))for(;e.insertBefore(o.cloneNode(!0),t),!(o===s||!(o=o.nextSibling)););else{Yr.innerHTML=Ts(r==="svg"?`<svg>${n}</svg>`:r==="mathml"?`<math>${n}</math>`:n);const a=Yr.content;if(r==="svg"||r==="mathml"){const l=a.firstChild;for(;l.firstChild;)a.appendChild(l.firstChild);a.removeChild(l)}e.insertBefore(a,t)}return[i?i.nextSibling:e.firstChild,t?t.previousSibling:e.lastChild]}},Ya=Symbol("_vtc");function Qa(n,e,t){const r=n[Ya];r&&(e=(e?[e,...r]:[...r]).join(" ")),e==null?n.removeAttribute("class"):t?n.setAttribute("class",e):n.className=e}const Qr=Symbol("_vod"),Za=Symbol("_vsh"),Xa=Symbol(""),nl=/(?:^|;)\s*display\s*:/;function el(n,e,t){const r=n.style,o=an(t);let s=!1;if(t&&!o){if(e)if(an(e))for(const i of e.split(";")){const a=i.slice(0,i.indexOf(":")).trim();t[a]==null&&ut(r,a,"")}else for(const i in e)t[i]==null&&ut(r,i,"");for(const i in t)i==="display"&&(s=!0),ut(r,i,t[i])}else if(o){if(e!==t){const i=r[Xa];i&&(t+=";"+i),r.cssText=t,s=nl.test(t)}}else e&&n.removeAttribute("style");Qr in n&&(n[Qr]=s?r.display:"",n[Za]&&(r.display="none"))}const Zr=/\s*!important$/;function ut(n,e,t){if(H(t))t.forEach(r=>ut(n,e,r));else if(t==null&&(t=""),e.startsWith("--"))n.setProperty(e,t);else{const r=tl(n,e);Zr.test(t)?n.setProperty(ve(r),t.replace(Zr,""),"important"):n[r]=t}}const Xr=["Webkit","Moz","ms"],Ht={};function tl(n,e){const t=Ht[e];if(t)return t;let r=_n(e);if(r!=="filter"&&r in n)return Ht[e]=r;r=kt(r);for(let o=0;o<Xr.length;o++){const s=Xr[o]+r;if(s in n)return Ht[e]=s}return e}const no="http://www.w3.org/1999/xlink";function eo(n,e,t,r,o,s=oi(e)){r&&e.startsWith("xlink:")?t==null?n.removeAttributeNS(no,e.slice(6,e.length)):n.setAttributeNS(no,e,t):t==null||s&&!So(t)?n.removeAttribute(e):n.setAttribute(e,s?"":Ne(t)?String(t):t)}function to(n,e,t,r,o){if(e==="innerHTML"||e==="textContent"){t!=null&&(n[e]=e==="innerHTML"?Ts(t):t);return}const s=n.tagName;if(e==="value"&&s!=="PROGRESS"&&!s.includes("-")){const a=s==="OPTION"?n.getAttribute("value")||"":n.value,l=t==null?n.type==="checkbox"?"on":"":String(t);(a!==l||!("_value"in n))&&(n.value=l),t==null&&n.removeAttribute(e),n._value=t;return}let i=!1;if(t===""||t==null){const a=typeof n[e];a==="boolean"?t=So(t):t==null&&a==="string"?(t="",i=!0):a==="number"&&(t=0,i=!0)}try{n[e]=t}catch{}i&&n.removeAttribute(o||e)}function rl(n,e,t,r){n.addEventListener(e,t,r)}function ol(n,e,t,r){n.removeEventListener(e,t,r)}const ro=Symbol("_vei");function sl(n,e,t,r,o=null){const s=n[ro]||(n[ro]={}),i=s[e];if(r&&i)i.value=r;else{const[a,l]=il(e);if(r){const f=s[e]=cl(r,o);rl(n,a,f,l)}else i&&(ol(n,a,i,l),s[e]=void 0)}}const oo=/(?:Once|Passive|Capture)$/;function il(n){let e;if(oo.test(n)){e={};let r;for(;r=n.match(oo);)n=n.slice(0,n.length-r[0].length),e[r[0].toLowerCase()]=!0}return[n[2]===":"?n.slice(3):ve(n.slice(2)),e]}let Ft=0;const al=Promise.resolve(),ll=()=>Ft||(al.then(()=>Ft=0),Ft=Date.now());function cl(n,e){const t=r=>{if(!r._vts)r._vts=Date.now();else if(r._vts<=t.attached)return;$n(dl(r,t.value),e,5,[r])};return t.value=n,t.attached=ll(),t}function dl(n,e){if(H(e)){const t=n.stopImmediatePropagation;return n.stopImmediatePropagation=()=>{t.call(n),n._stopped=!0},e.map(r=>o=>!o._stopped&&r&&r(o))}else return e}const so=n=>n.charCodeAt(0)===111&&n.charCodeAt(1)===110&&n.charCodeAt(2)>96&&n.charCodeAt(2)<123,ul=(n,e,t,r,o,s)=>{const i=o==="svg";e==="class"?Qa(n,r,i):e==="style"?el(n,t,r):xt(e)?lr(e)||sl(n,e,t,r,s):(e[0]==="."?(e=e.slice(1),!0):e[0]==="^"?(e=e.slice(1),!1):pl(n,e,r,i))?(to(n,e,r),!n.tagName.includes("-")&&(e==="value"||e==="checked"||e==="selected")&&eo(n,e,r,i,s,e!=="value")):n._isVueCE&&(/[A-Z]/.test(e)||!an(r))?to(n,_n(e),r,s,e):(e==="true-value"?n._trueValue=r:e==="false-value"&&(n._falseValue=r),eo(n,e,r,i))};function pl(n,e,t,r){if(r)return!!(e==="innerHTML"||e==="textContent"||e in n&&so(e)&&j(t));if(e==="spellcheck"||e==="draggable"||e==="translate"||e==="autocorrect"||e==="sandbox"&&n.tagName==="IFRAME"||e==="form"||e==="list"&&n.tagName==="INPUT"||e==="type"&&n.tagName==="TEXTAREA")return!1;if(e==="width"||e==="height"){const o=n.tagName;if(o==="IMG"||o==="VIDEO"||o==="CANVAS"||o==="SOURCE")return!1}return so(e)&&an(t)?!1:e in n}const fl=on({patchProp:ul},Ja);let io;function ml(){return io||(io=Ca(fl))}const gl=(...n)=>{const e=ml().createApp(...n),{mount:t}=e;return e.mount=r=>{const o=hl(r);if(!o)return;const s=e._component;!j(s)&&!s.render&&!s.template&&(s.template=o.innerHTML),o.nodeType===1&&(o.textContent="");const i=t(o,!1,bl(o));return o instanceof Element&&(o.removeAttribute("v-cloak"),o.setAttribute("data-v-app","")),i},e};function bl(n){if(n instanceof SVGElement)return"svg";if(typeof MathMLElement=="function"&&n instanceof MathMLElement)return"mathml"}function hl(n){return an(n)?document.querySelector(n):n}/*!
 * vue-router v4.6.4
 * (c) 2025 Eduardo San Martin Morote
 * @license MIT
 */const Ce=typeof document<"u";function Ls(n){return typeof n=="object"||"displayName"in n||"props"in n||"__vccOpts"in n}function vl(n){return n.__esModule||n[Symbol.toStringTag]==="Module"||n.default&&Ls(n.default)}const V=Object.assign;function zt(n,e){const t={};for(const r in e){const o=e[r];t[r]=An(o)?o.map(n):n(o)}return t}const Je=()=>{},An=Array.isArray;function ao(n,e){const t={};for(const r in n)t[r]=r in e?e[r]:n[r];return t}const Ps=/#/g,yl=/&/g,xl=/\//g,wl=/=/g,El=/\?/g,As=/\+/g,kl=/%5B/g,Cl=/%5D/g,Os=/%5E/g,_l=/%60/g,Ns=/%7B/g,Il=/%7C/g,Rs=/%7D/g,Sl=/%20/g;function Br(n){return n==null?"":encodeURI(""+n).replace(Il,"|").replace(kl,"[").replace(Cl,"]")}function Bl(n){return Br(n).replace(Ns,"{").replace(Rs,"}").replace(Os,"^")}function rr(n){return Br(n).replace(As,"%2B").replace(Sl,"+").replace(Ps,"%23").replace(yl,"%26").replace(_l,"`").replace(Ns,"{").replace(Rs,"}").replace(Os,"^")}function Tl(n){return rr(n).replace(wl,"%3D")}function Ll(n){return Br(n).replace(Ps,"%23").replace(El,"%3F")}function Pl(n){return Ll(n).replace(xl,"%2F")}function tt(n){if(n==null)return null;try{return decodeURIComponent(""+n)}catch{}return""+n}const Al=/\/$/,Ol=n=>n.replace(Al,"");function $t(n,e,t="/"){let r,o={},s="",i="";const a=e.indexOf("#");let l=e.indexOf("?");return l=a>=0&&l>a?-1:l,l>=0&&(r=e.slice(0,l),s=e.slice(l,a>0?a:e.length),o=n(s.slice(1))),a>=0&&(r=r||e.slice(0,a),i=e.slice(a,e.length)),r=Dl(r??e,t),{fullPath:r+s+i,path:r,query:o,hash:tt(i)}}function Nl(n,e){const t=e.query?n(e.query):"";return e.path+(t&&"?")+t+(e.hash||"")}function lo(n,e){return!e||!n.toLowerCase().startsWith(e.toLowerCase())?n:n.slice(e.length)||"/"}function Rl(n,e,t){const r=e.matched.length-1,o=t.matched.length-1;return r>-1&&r===o&&Ae(e.matched[r],t.matched[o])&&Ms(e.params,t.params)&&n(e.query)===n(t.query)&&e.hash===t.hash}function Ae(n,e){return(n.aliasOf||n)===(e.aliasOf||e)}function Ms(n,e){if(Object.keys(n).length!==Object.keys(e).length)return!1;for(var t in n)if(!Ml(n[t],e[t]))return!1;return!0}function Ml(n,e){return An(n)?co(n,e):An(e)?co(e,n):(n==null?void 0:n.valueOf())===(e==null?void 0:e.valueOf())}function co(n,e){return An(e)?n.length===e.length&&n.every((t,r)=>t===e[r]):n.length===1&&n[0]===e}function Dl(n,e){if(n.startsWith("/"))return n;if(!n)return e;const t=e.split("/"),r=n.split("/"),o=r[r.length-1];(o===".."||o===".")&&r.push("");let s=t.length-1,i,a;for(i=0;i<r.length;i++)if(a=r[i],a!==".")if(a==="..")s>1&&s--;else break;return t.slice(0,s).join("/")+"/"+r.slice(i).join("/")}const re={path:"/",name:void 0,params:{},query:{},hash:"",fullPath:"/",matched:[],meta:{},redirectedFrom:void 0};let or=function(n){return n.pop="pop",n.push="push",n}({}),Vt=function(n){return n.back="back",n.forward="forward",n.unknown="",n}({});function Ul(n){if(!n)if(Ce){const e=document.querySelector("base");n=e&&e.getAttribute("href")||"/",n=n.replace(/^\w+:\/\/[^\/]+/,"")}else n="/";return n[0]!=="/"&&n[0]!=="#"&&(n="/"+n),Ol(n)}const jl=/^[^#]+#/;function Hl(n,e){return n.replace(jl,"#")+e}function Fl(n,e){const t=document.documentElement.getBoundingClientRect(),r=n.getBoundingClientRect();return{behavior:e.behavior,left:r.left-t.left-(e.left||0),top:r.top-t.top-(e.top||0)}}const Lt=()=>({left:window.scrollX,top:window.scrollY});function zl(n){let e;if("el"in n){const t=n.el,r=typeof t=="string"&&t.startsWith("#"),o=typeof t=="string"?r?document.getElementById(t.slice(1)):document.querySelector(t):t;if(!o)return;e=Fl(o,n)}else e=n;"scrollBehavior"in document.documentElement.style?window.scrollTo(e):window.scrollTo(e.left!=null?e.left:window.scrollX,e.top!=null?e.top:window.scrollY)}function uo(n,e){return(history.state?history.state.position-e:-1)+n}const sr=new Map;function $l(n,e){sr.set(n,e)}function Vl(n){const e=sr.get(n);return sr.delete(n),e}function ql(n){return typeof n=="string"||n&&typeof n=="object"}function Ds(n){return typeof n=="string"||typeof n=="symbol"}let en=function(n){return n[n.MATCHER_NOT_FOUND=1]="MATCHER_NOT_FOUND",n[n.NAVIGATION_GUARD_REDIRECT=2]="NAVIGATION_GUARD_REDIRECT",n[n.NAVIGATION_ABORTED=4]="NAVIGATION_ABORTED",n[n.NAVIGATION_CANCELLED=8]="NAVIGATION_CANCELLED",n[n.NAVIGATION_DUPLICATED=16]="NAVIGATION_DUPLICATED",n}({});const Us=Symbol("");en.MATCHER_NOT_FOUND+"",en.NAVIGATION_GUARD_REDIRECT+"",en.NAVIGATION_ABORTED+"",en.NAVIGATION_CANCELLED+"",en.NAVIGATION_DUPLICATED+"";function Oe(n,e){return V(new Error,{type:n,[Us]:!0},e)}function Gn(n,e){return n instanceof Error&&Us in n&&(e==null||!!(n.type&e))}const Gl=["params","query","hash"];function Kl(n){if(typeof n=="string")return n;if(n.path!=null)return n.path;const e={};for(const t of Gl)t in n&&(e[t]=n[t]);return JSON.stringify(e,null,2)}function Wl(n){const e={};if(n===""||n==="?")return e;const t=(n[0]==="?"?n.slice(1):n).split("&");for(let r=0;r<t.length;++r){const o=t[r].replace(As," "),s=o.indexOf("="),i=tt(s<0?o:o.slice(0,s)),a=s<0?null:tt(o.slice(s+1));if(i in e){let l=e[i];An(l)||(l=e[i]=[l]),l.push(a)}else e[i]=a}return e}function po(n){let e="";for(let t in n){const r=n[t];if(t=Tl(t),r==null){r!==void 0&&(e+=(e.length?"&":"")+t);continue}(An(r)?r.map(o=>o&&rr(o)):[r&&rr(r)]).forEach(o=>{o!==void 0&&(e+=(e.length?"&":"")+t,o!=null&&(e+="="+o))})}return e}function Jl(n){const e={};for(const t in n){const r=n[t];r!==void 0&&(e[t]=An(r)?r.map(o=>o==null?null:""+o):r==null?r:""+r)}return e}const Yl=Symbol(""),fo=Symbol(""),Tr=Symbol(""),js=Symbol(""),ir=Symbol("");function je(){let n=[];function e(r){return n.push(r),()=>{const o=n.indexOf(r);o>-1&&n.splice(o,1)}}function t(){n=[]}return{add:e,list:()=>n.slice(),reset:t}}function ae(n,e,t,r,o,s=i=>i()){const i=r&&(r.enterCallbacks[o]=r.enterCallbacks[o]||[]);return()=>new Promise((a,l)=>{const f=m=>{m===!1?l(Oe(en.NAVIGATION_ABORTED,{from:t,to:e})):m instanceof Error?l(m):ql(m)?l(Oe(en.NAVIGATION_GUARD_REDIRECT,{from:e,to:m})):(i&&r.enterCallbacks[o]===i&&typeof m=="function"&&i.push(m),a())},u=s(()=>n.call(r&&r.instances[o],e,t,f));let p=Promise.resolve(u);n.length<3&&(p=p.then(f)),p.catch(m=>l(m))})}function qt(n,e,t,r,o=s=>s()){const s=[];for(const i of n)for(const a in i.components){let l=i.components[a];if(!(e!=="beforeRouteEnter"&&!i.instances[a]))if(Ls(l)){const f=(l.__vccOpts||l)[e];f&&s.push(ae(f,t,r,i,a,o))}else{let f=l();s.push(()=>f.then(u=>{if(!u)throw new Error(`Couldn't resolve component "${a}" at "${i.path}"`);const p=vl(u)?u.default:u;i.mods[a]=u,i.components[a]=p;const m=(p.__vccOpts||p)[e];return m&&ae(m,t,r,i,a,o)()}))}}return s}function Ql(n,e){const t=[],r=[],o=[],s=Math.max(e.matched.length,n.matched.length);for(let i=0;i<s;i++){const a=e.matched[i];a&&(n.matched.find(f=>Ae(f,a))?r.push(a):t.push(a));const l=n.matched[i];l&&(e.matched.find(f=>Ae(f,l))||o.push(l))}return[t,r,o]}/*!
 * vue-router v4.6.4
 * (c) 2025 Eduardo San Martin Morote
 * @license MIT
 */let Zl=()=>location.protocol+"//"+location.host;function Hs(n,e){const{pathname:t,search:r,hash:o}=e,s=n.indexOf("#");if(s>-1){let i=o.includes(n.slice(s))?n.slice(s).length:1,a=o.slice(i);return a[0]!=="/"&&(a="/"+a),lo(a,"")}return lo(t,n)+r+o}function Xl(n,e,t,r){let o=[],s=[],i=null;const a=({state:m})=>{const b=Hs(n,location),_=t.value,B=e.value;let U=0;if(m){if(t.value=b,e.value=m,i&&i===_){i=null;return}U=B?m.position-B.position:0}else r(b);o.forEach(O=>{O(t.value,_,{delta:U,type:or.pop,direction:U?U>0?Vt.forward:Vt.back:Vt.unknown})})};function l(){i=t.value}function f(m){o.push(m);const b=()=>{const _=o.indexOf(m);_>-1&&o.splice(_,1)};return s.push(b),b}function u(){if(document.visibilityState==="hidden"){const{history:m}=window;if(!m.state)return;m.replaceState(V({},m.state,{scroll:Lt()}),"")}}function p(){for(const m of s)m();s=[],window.removeEventListener("popstate",a),window.removeEventListener("pagehide",u),document.removeEventListener("visibilitychange",u)}return window.addEventListener("popstate",a),window.addEventListener("pagehide",u),document.addEventListener("visibilitychange",u),{pauseListeners:l,listen:f,destroy:p}}function mo(n,e,t,r=!1,o=!1){return{back:n,current:e,forward:t,replaced:r,position:window.history.length,scroll:o?Lt():null}}function nc(n){const{history:e,location:t}=window,r={value:Hs(n,t)},o={value:e.state};o.value||s(r.value,{back:null,current:r.value,forward:null,position:e.length-1,replaced:!0,scroll:null},!0);function s(l,f,u){const p=n.indexOf("#"),m=p>-1?(t.host&&document.querySelector("base")?n:n.slice(p))+l:Zl()+n+l;try{e[u?"replaceState":"pushState"](f,"",m),o.value=f}catch(b){console.error(b),t[u?"replace":"assign"](m)}}function i(l,f){s(l,V({},e.state,mo(o.value.back,l,o.value.forward,!0),f,{position:o.value.position}),!0),r.value=l}function a(l,f){const u=V({},o.value,e.state,{forward:l,scroll:Lt()});s(u.current,u,!0),s(l,V({},mo(r.value,l,null),{position:u.position+1},f),!1),r.value=l}return{location:r,state:o,push:a,replace:i}}function ec(n){n=Ul(n);const e=nc(n),t=Xl(n,e.state,e.location,e.replace);function r(s,i=!0){i||t.pauseListeners(),history.go(s)}const o=V({location:"",base:n,go:r,createHref:Hl.bind(null,n)},e,t);return Object.defineProperty(o,"location",{enumerable:!0,get:()=>e.location.value}),Object.defineProperty(o,"state",{enumerable:!0,get:()=>e.state.value}),o}let be=function(n){return n[n.Static=0]="Static",n[n.Param=1]="Param",n[n.Group=2]="Group",n}({});var rn=function(n){return n[n.Static=0]="Static",n[n.Param=1]="Param",n[n.ParamRegExp=2]="ParamRegExp",n[n.ParamRegExpEnd=3]="ParamRegExpEnd",n[n.EscapeNext=4]="EscapeNext",n}(rn||{});const tc={type:be.Static,value:""},rc=/[a-zA-Z0-9_]/;function oc(n){if(!n)return[[]];if(n==="/")return[[tc]];if(!n.startsWith("/"))throw new Error(`Invalid path "${n}"`);function e(b){throw new Error(`ERR (${t})/"${f}": ${b}`)}let t=rn.Static,r=t;const o=[];let s;function i(){s&&o.push(s),s=[]}let a=0,l,f="",u="";function p(){f&&(t===rn.Static?s.push({type:be.Static,value:f}):t===rn.Param||t===rn.ParamRegExp||t===rn.ParamRegExpEnd?(s.length>1&&(l==="*"||l==="+")&&e(`A repeatable param (${f}) must be alone in its segment. eg: '/:ids+.`),s.push({type:be.Param,value:f,regexp:u,repeatable:l==="*"||l==="+",optional:l==="*"||l==="?"})):e("Invalid state to consume buffer"),f="")}function m(){f+=l}for(;a<n.length;){if(l=n[a++],l==="\\"&&t!==rn.ParamRegExp){r=t,t=rn.EscapeNext;continue}switch(t){case rn.Static:l==="/"?(f&&p(),i()):l===":"?(p(),t=rn.Param):m();break;case rn.EscapeNext:m(),t=r;break;case rn.Param:l==="("?t=rn.ParamRegExp:rc.test(l)?m():(p(),t=rn.Static,l!=="*"&&l!=="?"&&l!=="+"&&a--);break;case rn.ParamRegExp:l===")"?u[u.length-1]=="\\"?u=u.slice(0,-1)+l:t=rn.ParamRegExpEnd:u+=l;break;case rn.ParamRegExpEnd:p(),t=rn.Static,l!=="*"&&l!=="?"&&l!=="+"&&a--,u="";break;default:e("Unknown state");break}}return t===rn.ParamRegExp&&e(`Unfinished custom RegExp for param "${f}"`),p(),i(),o}const go="[^/]+?",sc={sensitive:!1,strict:!1,start:!0,end:!0};var mn=function(n){return n[n._multiplier=10]="_multiplier",n[n.Root=90]="Root",n[n.Segment=40]="Segment",n[n.SubSegment=30]="SubSegment",n[n.Static=40]="Static",n[n.Dynamic=20]="Dynamic",n[n.BonusCustomRegExp=10]="BonusCustomRegExp",n[n.BonusWildcard=-50]="BonusWildcard",n[n.BonusRepeatable=-20]="BonusRepeatable",n[n.BonusOptional=-8]="BonusOptional",n[n.BonusStrict=.7000000000000001]="BonusStrict",n[n.BonusCaseSensitive=.25]="BonusCaseSensitive",n}(mn||{});const ic=/[.+*?^${}()[\]/\\]/g;function ac(n,e){const t=V({},sc,e),r=[];let o=t.start?"^":"";const s=[];for(const f of n){const u=f.length?[]:[mn.Root];t.strict&&!f.length&&(o+="/");for(let p=0;p<f.length;p++){const m=f[p];let b=mn.Segment+(t.sensitive?mn.BonusCaseSensitive:0);if(m.type===be.Static)p||(o+="/"),o+=m.value.replace(ic,"\\$&"),b+=mn.Static;else if(m.type===be.Param){const{value:_,repeatable:B,optional:U,regexp:O}=m;s.push({name:_,repeatable:B,optional:U});const L=O||go;if(L!==go){b+=mn.BonusCustomRegExp;try{`${L}`}catch(P){throw new Error(`Invalid custom RegExp for param "${_}" (${L}): `+P.message)}}let R=B?`((?:${L})(?:/(?:${L}))*)`:`(${L})`;p||(R=U&&f.length<2?`(?:/${R})`:"/"+R),U&&(R+="?"),o+=R,b+=mn.Dynamic,U&&(b+=mn.BonusOptional),B&&(b+=mn.BonusRepeatable),L===".*"&&(b+=mn.BonusWildcard)}u.push(b)}r.push(u)}if(t.strict&&t.end){const f=r.length-1;r[f][r[f].length-1]+=mn.BonusStrict}t.strict||(o+="/?"),t.end?o+="$":t.strict&&!o.endsWith("/")&&(o+="(?:/|$)");const i=new RegExp(o,t.sensitive?"":"i");function a(f){const u=f.match(i),p={};if(!u)return null;for(let m=1;m<u.length;m++){const b=u[m]||"",_=s[m-1];p[_.name]=b&&_.repeatable?b.split("/"):b}return p}function l(f){let u="",p=!1;for(const m of n){(!p||!u.endsWith("/"))&&(u+="/"),p=!1;for(const b of m)if(b.type===be.Static)u+=b.value;else if(b.type===be.Param){const{value:_,repeatable:B,optional:U}=b,O=_ in f?f[_]:"";if(An(O)&&!B)throw new Error(`Provided param "${_}" is an array but it is not repeatable (* or + modifiers)`);const L=An(O)?O.join("/"):O;if(!L)if(U)m.length<2&&(u.endsWith("/")?u=u.slice(0,-1):p=!0);else throw new Error(`Missing required param "${_}"`);u+=L}}return u||"/"}return{re:i,score:r,keys:s,parse:a,stringify:l}}function lc(n,e){let t=0;for(;t<n.length&&t<e.length;){const r=e[t]-n[t];if(r)return r;t++}return n.length<e.length?n.length===1&&n[0]===mn.Static+mn.Segment?-1:1:n.length>e.length?e.length===1&&e[0]===mn.Static+mn.Segment?1:-1:0}function Fs(n,e){let t=0;const r=n.score,o=e.score;for(;t<r.length&&t<o.length;){const s=lc(r[t],o[t]);if(s)return s;t++}if(Math.abs(o.length-r.length)===1){if(bo(r))return 1;if(bo(o))return-1}return o.length-r.length}function bo(n){const e=n[n.length-1];return n.length>0&&e[e.length-1]<0}const cc={strict:!1,end:!0,sensitive:!1};function dc(n,e,t){const r=ac(oc(n.path),t),o=V(r,{record:n,parent:e,children:[],alias:[]});return e&&!o.record.aliasOf==!e.record.aliasOf&&e.children.push(o),o}function uc(n,e){const t=[],r=new Map;e=ao(cc,e);function o(p){return r.get(p)}function s(p,m,b){const _=!b,B=vo(p);B.aliasOf=b&&b.record;const U=ao(e,p),O=[B];if("alias"in p){const P=typeof p.alias=="string"?[p.alias]:p.alias;for(const J of P)O.push(vo(V({},B,{components:b?b.record.components:B.components,path:J,aliasOf:b?b.record:B})))}let L,R;for(const P of O){const{path:J}=P;if(m&&J[0]!=="/"){const ln=m.record.path,nn=ln[ln.length-1]==="/"?"":"/";P.path=m.record.path+(J&&nn+J)}if(L=dc(P,m,U),b?b.alias.push(L):(R=R||L,R!==L&&R.alias.push(L),_&&p.name&&!yo(L)&&i(p.name)),zs(L)&&l(L),B.children){const ln=B.children;for(let nn=0;nn<ln.length;nn++)s(ln[nn],L,b&&b.children[nn])}b=b||L}return R?()=>{i(R)}:Je}function i(p){if(Ds(p)){const m=r.get(p);m&&(r.delete(p),t.splice(t.indexOf(m),1),m.children.forEach(i),m.alias.forEach(i))}else{const m=t.indexOf(p);m>-1&&(t.splice(m,1),p.record.name&&r.delete(p.record.name),p.children.forEach(i),p.alias.forEach(i))}}function a(){return t}function l(p){const m=mc(p,t);t.splice(m,0,p),p.record.name&&!yo(p)&&r.set(p.record.name,p)}function f(p,m){let b,_={},B,U;if("name"in p&&p.name){if(b=r.get(p.name),!b)throw Oe(en.MATCHER_NOT_FOUND,{location:p});U=b.record.name,_=V(ho(m.params,b.keys.filter(R=>!R.optional).concat(b.parent?b.parent.keys.filter(R=>R.optional):[]).map(R=>R.name)),p.params&&ho(p.params,b.keys.map(R=>R.name))),B=b.stringify(_)}else if(p.path!=null)B=p.path,b=t.find(R=>R.re.test(B)),b&&(_=b.parse(B),U=b.record.name);else{if(b=m.name?r.get(m.name):t.find(R=>R.re.test(m.path)),!b)throw Oe(en.MATCHER_NOT_FOUND,{location:p,currentLocation:m});U=b.record.name,_=V({},m.params,p.params),B=b.stringify(_)}const O=[];let L=b;for(;L;)O.unshift(L.record),L=L.parent;return{name:U,path:B,params:_,matched:O,meta:fc(O)}}n.forEach(p=>s(p));function u(){t.length=0,r.clear()}return{addRoute:s,resolve:f,removeRoute:i,clearRoutes:u,getRoutes:a,getRecordMatcher:o}}function ho(n,e){const t={};for(const r of e)r in n&&(t[r]=n[r]);return t}function vo(n){const e={path:n.path,redirect:n.redirect,name:n.name,meta:n.meta||{},aliasOf:n.aliasOf,beforeEnter:n.beforeEnter,props:pc(n),children:n.children||[],instances:{},leaveGuards:new Set,updateGuards:new Set,enterCallbacks:{},components:"components"in n?n.components||null:n.component&&{default:n.component}};return Object.defineProperty(e,"mods",{value:{}}),e}function pc(n){const e={},t=n.props||!1;if("component"in n)e.default=t;else for(const r in n.components)e[r]=typeof t=="object"?t[r]:t;return e}function yo(n){for(;n;){if(n.record.aliasOf)return!0;n=n.parent}return!1}function fc(n){return n.reduce((e,t)=>V(e,t.meta),{})}function mc(n,e){let t=0,r=e.length;for(;t!==r;){const s=t+r>>1;Fs(n,e[s])<0?r=s:t=s+1}const o=gc(n);return o&&(r=e.lastIndexOf(o,r-1)),r}function gc(n){let e=n;for(;e=e.parent;)if(zs(e)&&Fs(n,e)===0)return e}function zs({record:n}){return!!(n.name||n.components&&Object.keys(n.components).length||n.redirect)}function xo(n){const e=Jn(Tr),t=Jn(js),r=Sn(()=>{const l=Se(n.to);return e.resolve(l)}),o=Sn(()=>{const{matched:l}=r.value,{length:f}=l,u=l[f-1],p=t.matched;if(!u||!p.length)return-1;const m=p.findIndex(Ae.bind(null,u));if(m>-1)return m;const b=wo(l[f-2]);return f>1&&wo(u)===b&&p[p.length-1].path!==b?p.findIndex(Ae.bind(null,l[f-2])):m}),s=Sn(()=>o.value>-1&&xc(t.params,r.value.params)),i=Sn(()=>o.value>-1&&o.value===t.matched.length-1&&Ms(t.params,r.value.params));function a(l={}){if(yc(l)){const f=e[Se(n.replace)?"replace":"push"](Se(n.to)).catch(Je);return n.viewTransition&&typeof document<"u"&&"startViewTransition"in document&&document.startViewTransition(()=>f),f}return Promise.resolve()}return{route:r,href:Sn(()=>r.value.href),isActive:s,isExactActive:i,navigate:a}}function bc(n){return n.length===1?n[0]:n}const hc=ns({name:"RouterLink",compatConfig:{MODE:3},props:{to:{type:[String,Object],required:!0},replace:Boolean,activeClass:String,exactActiveClass:String,custom:Boolean,ariaCurrentValue:{type:String,default:"page"},viewTransition:Boolean},useLink:xo,setup(n,{slots:e}){const t=_t(xo(n)),{options:r}=Jn(Tr),o=Sn(()=>({[Eo(n.activeClass,r.linkActiveClass,"router-link-active")]:t.isActive,[Eo(n.exactActiveClass,r.linkExactActiveClass,"router-link-exact-active")]:t.isExactActive}));return()=>{const s=e.default&&bc(e.default(t));return n.custom?s:Bs("a",{"aria-current":t.isExactActive?n.ariaCurrentValue:null,href:t.href,onClick:t.navigate,class:o.value},s)}}}),vc=hc;function yc(n){if(!(n.metaKey||n.altKey||n.ctrlKey||n.shiftKey)&&!n.defaultPrevented&&!(n.button!==void 0&&n.button!==0)){if(n.currentTarget&&n.currentTarget.getAttribute){const e=n.currentTarget.getAttribute("target");if(/\b_blank\b/i.test(e))return}return n.preventDefault&&n.preventDefault(),!0}}function xc(n,e){for(const t in e){const r=e[t],o=n[t];if(typeof r=="string"){if(r!==o)return!1}else if(!An(o)||o.length!==r.length||r.some((s,i)=>s.valueOf()!==o[i].valueOf()))return!1}return!0}function wo(n){return n?n.aliasOf?n.aliasOf.path:n.path:""}const Eo=(n,e,t)=>n??e??t,wc=ns({name:"RouterView",inheritAttrs:!1,props:{name:{type:String,default:"default"},route:Object},compatConfig:{MODE:3},setup(n,{attrs:e,slots:t}){const r=Jn(ir),o=Sn(()=>n.route||r.value),s=Jn(fo,0),i=Sn(()=>{let f=Se(s);const{matched:u}=o.value;let p;for(;(p=u[f])&&!p.components;)f++;return f}),a=Sn(()=>o.value.matched[i.value]);lt(fo,Sn(()=>i.value+1)),lt(Yl,a),lt(ir,o);const l=$o();return ct(()=>[l.value,a.value,n.name],([f,u,p],[m,b,_])=>{u&&(u.instances[p]=f,b&&b!==u&&f&&f===m&&(u.leaveGuards.size||(u.leaveGuards=b.leaveGuards),u.updateGuards.size||(u.updateGuards=b.updateGuards))),f&&u&&(!b||!Ae(u,b)||!m)&&(u.enterCallbacks[p]||[]).forEach(B=>B(f))},{flush:"post"}),()=>{const f=o.value,u=n.name,p=a.value,m=p&&p.components[u];if(!m)return ko(t.default,{Component:m,route:f});const b=p.props[u],_=b?b===!0?f.params:typeof b=="function"?b(f):b:null,U=Bs(m,V({},_,e,{onVnodeUnmounted:O=>{O.component.isUnmounted&&(p.instances[u]=null)},ref:l}));return ko(t.default,{Component:U,route:f})||U}}});function ko(n,e){if(!n)return null;const t=n(e);return t.length===1?t[0]:t}const Ec=wc;function kc(n){const e=uc(n.routes,n),t=n.parseQuery||Wl,r=n.stringifyQuery||po,o=n.history,s=je(),i=je(),a=je(),l=Ii(re);let f=re;Ce&&n.scrollBehavior&&"scrollRestoration"in history&&(history.scrollRestoration="manual");const u=zt.bind(null,v=>""+v),p=zt.bind(null,Pl),m=zt.bind(null,tt);function b(v,T){let I,N;return Ds(v)?(I=e.getRecordMatcher(v),N=T):N=v,e.addRoute(N,I)}function _(v){const T=e.getRecordMatcher(v);T&&e.removeRoute(T)}function B(){return e.getRoutes().map(v=>v.record)}function U(v){return!!e.getRecordMatcher(v)}function O(v,T){if(T=V({},T||l.value),typeof v=="string"){const d=$t(t,v,T.path),g=e.resolve({path:d.path},T),y=o.createHref(d.fullPath);return V(d,g,{params:m(g.params),hash:tt(d.hash),redirectedFrom:void 0,href:y})}let I;if(v.path!=null)I=V({},v,{path:$t(t,v.path,T.path).path});else{const d=V({},v.params);for(const g in d)d[g]==null&&delete d[g];I=V({},v,{params:p(d)}),T.params=p(T.params)}const N=e.resolve(I,T),F=v.hash||"";N.params=u(m(N.params));const X=Nl(r,V({},v,{hash:Bl(F),path:N.path})),c=o.createHref(X);return V({fullPath:X,hash:F,query:r===po?Jl(v.query):v.query||{}},N,{redirectedFrom:void 0,href:c})}function L(v){return typeof v=="string"?$t(t,v,l.value.path):V({},v)}function R(v,T){if(f!==v)return Oe(en.NAVIGATION_CANCELLED,{from:T,to:v})}function P(v){return nn(v)}function J(v){return P(V(L(v),{replace:!0}))}function ln(v,T){const I=v.matched[v.matched.length-1];if(I&&I.redirect){const{redirect:N}=I;let F=typeof N=="function"?N(v,T):N;return typeof F=="string"&&(F=F.includes("?")||F.includes("#")?F=L(F):{path:F},F.params={}),V({query:v.query,hash:v.hash,params:F.path!=null?{}:v.params},F)}}function nn(v,T){const I=f=O(v),N=l.value,F=v.state,X=v.force,c=v.replace===!0,d=ln(I,N);if(d)return nn(V(L(d),{state:typeof d=="object"?V({},F,d.state):F,force:X,replace:c}),T||I);const g=I;g.redirectedFrom=T;let y;return!X&&Rl(r,N,I)&&(y=Oe(en.NAVIGATION_DUPLICATED,{to:g,from:N}),Mn(N,N,!0,!1)),(y?Promise.resolve(y):Nn(g,N)).catch(h=>Gn(h)?Gn(h,en.NAVIGATION_GUARD_REDIRECT)?h:te(h):$(h,g,N)).then(h=>{if(h){if(Gn(h,en.NAVIGATION_GUARD_REDIRECT))return nn(V({replace:c},L(h.to),{state:typeof h.to=="object"?V({},F,h.to.state):F,force:X}),T||g)}else h=ue(g,N,!0,c,F);return ee(g,N,h),h})}function On(v,T){const I=R(v,T);return I?Promise.reject(I):Promise.resolve()}function ne(v){const T=we.values().next().value;return T&&typeof T.runWithContext=="function"?T.runWithContext(v):v()}function Nn(v,T){let I;const[N,F,X]=Ql(v,T);I=qt(N.reverse(),"beforeRouteLeave",v,T);for(const d of N)d.leaveGuards.forEach(g=>{I.push(ae(g,v,T))});const c=On.bind(null,v,T);return I.push(c),kn(I).then(()=>{I=[];for(const d of s.list())I.push(ae(d,v,T));return I.push(c),kn(I)}).then(()=>{I=qt(F,"beforeRouteUpdate",v,T);for(const d of F)d.updateGuards.forEach(g=>{I.push(ae(g,v,T))});return I.push(c),kn(I)}).then(()=>{I=[];for(const d of X)if(d.beforeEnter)if(An(d.beforeEnter))for(const g of d.beforeEnter)I.push(ae(g,v,T));else I.push(ae(d.beforeEnter,v,T));return I.push(c),kn(I)}).then(()=>(v.matched.forEach(d=>d.enterCallbacks={}),I=qt(X,"beforeRouteEnter",v,T,ne),I.push(c),kn(I))).then(()=>{I=[];for(const d of i.list())I.push(ae(d,v,T));return I.push(c),kn(I)}).catch(d=>Gn(d,en.NAVIGATION_CANCELLED)?d:Promise.reject(d))}function ee(v,T,I){a.list().forEach(N=>ne(()=>N(v,T,I)))}function ue(v,T,I,N,F){const X=R(v,T);if(X)return X;const c=T===re,d=Ce?history.state:{};I&&(N||c?o.replace(v.fullPath,V({scroll:c&&d&&d.scroll},F)):o.push(v.fullPath,F)),l.value=v,Mn(v,T,I,c),te()}let Rn;function Re(){Rn||(Rn=o.listen((v,T,I)=>{if(!pe.listening)return;const N=O(v),F=ln(N,pe.currentRoute.value);if(F){nn(V(F,{replace:!0,force:!0}),N).catch(Je);return}f=N;const X=l.value;Ce&&$l(uo(X.fullPath,I.delta),Lt()),Nn(N,X).catch(c=>Gn(c,en.NAVIGATION_ABORTED|en.NAVIGATION_CANCELLED)?c:Gn(c,en.NAVIGATION_GUARD_REDIRECT)?(nn(V(L(c.to),{force:!0}),N).then(d=>{Gn(d,en.NAVIGATION_ABORTED|en.NAVIGATION_DUPLICATED)&&!I.delta&&I.type===or.pop&&o.go(-1,!1)}).catch(Je),Promise.reject()):(I.delta&&o.go(-I.delta,!1),$(c,N,X))).then(c=>{c=c||ue(N,X,!1),c&&(I.delta&&!Gn(c,en.NAVIGATION_CANCELLED)?o.go(-I.delta,!1):I.type===or.pop&&Gn(c,en.NAVIGATION_ABORTED|en.NAVIGATION_DUPLICATED)&&o.go(-1,!1)),ee(N,X,c)}).catch(Je)}))}let ye=je(),sn=je(),W;function $(v,T,I){te(v);const N=sn.list();return N.length?N.forEach(F=>F(v,T,I)):console.error(v),Promise.reject(v)}function Vn(){return W&&l.value!==re?Promise.resolve():new Promise((v,T)=>{ye.add([v,T])})}function te(v){return W||(W=!v,Re(),ye.list().forEach(([T,I])=>v?I(v):T()),ye.reset()),v}function Mn(v,T,I,N){const{scrollBehavior:F}=n;if(!Ce||!F)return Promise.resolve();const X=!I&&Vl(uo(v.fullPath,0))||(N||!I)&&history.state&&history.state.scroll||null;return Ko().then(()=>F(v,T,X)).then(c=>c&&zl(c)).catch(c=>$(c,v,T))}const bn=v=>o.go(v);let xe;const we=new Set,pe={currentRoute:l,listening:!0,addRoute:b,removeRoute:_,clearRoutes:e.clearRoutes,hasRoute:U,getRoutes:B,resolve:O,options:n,push:P,replace:J,go:bn,back:()=>bn(-1),forward:()=>bn(1),beforeEach:s.add,beforeResolve:i.add,afterEach:a.add,onError:sn.add,isReady:Vn,install(v){v.component("RouterLink",vc),v.component("RouterView",Ec),v.config.globalProperties.$router=pe,Object.defineProperty(v.config.globalProperties,"$route",{enumerable:!0,get:()=>Se(l)}),Ce&&!xe&&l.value===re&&(xe=!0,P(o.location).catch(N=>{}));const T={};for(const N in re)Object.defineProperty(T,N,{get:()=>l.value[N],enumerable:!0});v.provide(Tr,pe),v.provide(js,zo(T)),v.provide(ir,l);const I=v.unmount;we.add(v),v.unmount=function(){we.delete(v),we.size<1&&(f=re,Rn&&Rn(),Rn=null,l.value=re,xe=!1,W=!1),I()}}};function kn(v){return v.reduce((T,I)=>T.then(()=>ne(I)),Promise.resolve())}return pe}const Cc=(n,e)=>{const t=n.__vccOpts||n;for(const[r,o]of e)t[r]=o;return t},_c={};function Ic(n,e){const t=na("router-view");return Es(),Pa(t)}const Sc=Cc(_c,[["render",Ic]]);const Bc=`<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <title>Club Booking Portal - Member Bookings</title>
  <link rel="stylesheet" href="theme.css">
  <style>
    body { margin: 0; background: #f8f9fa; }
    header { 
      position: sticky; 
      top: 0; 
      z-index: 10; 
      border-bottom: 1px solid rgba(15, 23, 42, 0.06); 
      background: #fff;
    }
    .wrap { max-width: 1100px; margin: 0 auto; padding: 16px; }
    .header-wrap { 
      display: flex; 
      align-items: center; 
      justify-content: space-between; 
      gap: 12px; 
    }
    .header-actions { 
      display: flex; 
      align-items: center; 
      gap: 8px; 
    }
    .site-title-link { 
      color: inherit; 
      text-decoration: none; 
      font-weight: 700;
    }
    .page-head { 
      display: flex; 
      align-items: center; 
      justify-content: space-between; 
      gap: 16px; 
      margin: 20px 0 16px; 
      flex-wrap: wrap;
    }
    .page-head h2 { 
      margin: 0; 
      font-size: 28px;
    }
    .page-head p { 
      margin: 4px 0 0; 
      color: var(--muted);
    }
    .card .pad { padding: 16px; }
    .stat-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(140px, 1fr)); gap: 12px; }
    .stat-card { border: 1px solid var(--line); border-radius: 12px; padding: 12px; background: #fff; }
    .stat-label { display: block; font-size: 12px; color: var(--muted); }
    .stat-value { display: block; font-size: 20px; font-weight: 700; margin-top: 4px; }
    .filter-row { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 12px; }
    .filter-btn { 
      border: none; 
      background: transparent; 
      color: var(--muted); 
      padding: 8px 14px;
      border-radius: 8px;
      font-size: 13px;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.2s ease;
    }
    .filter-btn:hover {
      background: rgba(15, 23, 42, 0.06);
      color: var(--ink);
    }
    .filter-btn.active { 
      background: #2563eb;
      color: #fff;
    }
    .list { margin-top: 16px; display: grid; gap: 12px; }
    .booking-card { 
      display: grid;
      grid-template-columns: 1fr auto 1fr;
      align-items: center;
      gap: 24px;
      padding: 16px;
      background: #fff;
      border: 1px solid rgba(15, 23, 42, 0.06);
      border-radius: 14px;
      transition: all 0.2s ease;
    }
    .booking-card:hover {
      border-color: rgba(15, 23, 42, 0.12);
      background: #fafbfc;
    }
    .booking-meta { display: grid; gap: 4px; }
    .booking-meta:nth-child(2) {
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .booking-meta:nth-child(3) {
      justify-self: end;
    }
    .booking-actions { 
      display: flex; 
      gap: 8px; 
      flex-wrap: wrap;
      justify-content: flex-end;
    }
    .status { 
      font-size: 12px; 
      font-weight: 600; 
      padding: 6px 12px; 
      border-radius: 8px; 
      border: none;
      text-align: left;
      width: 100px;
      display: inline-block;
    }
    .status.pending { background: #fef3c7; color: #b45309; }
    .status.approved { background: #dcfce7; color: #16a34a; }
    .status.checked { background: #dbeafe; color: #2563eb; }
    .status.cancelled { background: #fee2e2; color: #dc2626; }
    .booking-title { font-weight: 700; font-size: 15px; }
    .booking-sub { color: var(--muted); font-size: 13px; }
    .group-section {
      margin-bottom: 24px;
    }
    .group-header {
      font-size: 16px;
      font-weight: 700;
      margin-bottom: 12px;
      padding: 10px 14px;
      background: #f8f9fa;
      border-radius: 8px;
      color: var(--ink);
    }
    .group-list {
      display: grid;
      gap: 12px;
    }
    .btn {
      border-radius: 8px;
      padding: 8px 14px;
      font-size: 13px;
      font-weight: 600;
      text-decoration: none;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      border: none;
      background: #f1f3f5;
      color: #1e293b;
      cursor: pointer;
      transition: all 0.2s ease;
      box-shadow: none;
    }
    .btn:hover {
      background: #e3e6e9;
      transform: translateY(0);
      box-shadow: none;
    }
    .btn:active {
      background: #d4d8dc;
      transform: translateY(0);
      box-shadow: none;
    }
    .btn.blue {
      background: #f1f3f5;
      color: #1e293b;
    }
    .btn.blue:hover {
      background: #e3e6e9;
      box-shadow: none;
    }
    .btn.approve {
      background: #f1f3f5;
      color: #1e293b;
    }
    .btn.approve:hover {
      background: #e3e6e9;
      box-shadow: none;
    }
    .btn.checkin {
      background: #f1f3f5;
      color: #1e293b;
    }
    .btn.checkin:hover {
      background: #e3e6e9;
      box-shadow: none;
    }
    .btn.cancel {
      background: #f1f3f5;
      color: #1e293b;
    }
    .btn.cancel:hover {
      background: #e3e6e9;
      box-shadow: none;
    }
    .btn.ghost {
      background: transparent;
      color: var(--muted);
      border: 1px solid rgba(15, 23, 42, 0.1);
    }
    .btn.ghost:hover {
      color: var(--ink);
      background: rgba(15, 23, 42, 0.04);
    }
    .btn.small { padding: 6px 12px; font-size: 12px; }
    .info-overlay {
      position: fixed;
      inset: 0;
      display: none;
      align-items: center;
      justify-content: center;
      padding: 24px;
      background: rgba(10, 12, 16, 0.45);
      z-index: 3000;
    }
    .info-overlay.open { display: flex; }
    .info-card {
      position: relative;
      width: min(560px, 92vw);
      background: #fff;
      border-radius: 16px;
      box-shadow: none;
      padding: 20px 20px 18px;
    }
    .info-card h3 { margin: 0 0 8px; font-size: 18px; }
    .info-body p { margin: 0 0 10px; color: var(--muted); font-size: 13px; }
    .info-close {
      position: absolute;
      top: 10px;
      right: 10px;
      width: 30px;
      height: 30px;
      border-radius: 999px;
      border: 1px solid var(--line);
      background: #fff;
      cursor: pointer;
      color: var(--muted);
    }
    @media (max-width: 720px) {
      .booking-card { flex-direction: column; align-items: flex-start; }
      .booking-actions { width: 100%; }
      .header-actions { flex-wrap: wrap; }
    }
  </style>
</head>
<body>
  <header>
    <div class="wrap header-wrap" style="display: flex; align-items: center; justify-content: space-between; gap: 16px;">
      <div style="display: flex; align-items: center; gap: 16px;">
        <button onclick="window.parent.location.href = '/club-home'" type="button" style="border: none; background: none; cursor: pointer; font-size: 14px; color: #0066cc; text-decoration: none; padding: 8px 0;">← Back</button>
        <h2 style="margin: 0; font-size: 18px; font-weight: 600;">Member bookings</h2>
      </div>
      <div class="header-actions">
        <button id="logoutBtn" class="btn blue" type="button" style="min-width:80px">Logout</button>
      </div>
    </div>
  </header>

  <main class="wrap">
    <div class="page-head" style="margin-top: 0;">
      <div>
        <h2>Member bookings</h2>
        <p class="muted">Approve, check in, or cancel member reservations.</p>
      </div>
      <button id="refreshBtn" class="btn" type="button" style="min-width:100px">Refresh</button>
    </div>

    <section class="card">
      <div class="pad">
        <div class="stat-grid" id="stats">
          <div class="stat-card">
            <span class="stat-label">Pending</span>
            <span class="stat-value" data-stat="pending">0</span>
          </div>
          <div class="stat-card">
            <span class="stat-label">Approved</span>
            <span class="stat-value" data-stat="approved">0</span>
          </div>
          <div class="stat-card">
            <span class="stat-label">Checked-in</span>
            <span class="stat-value" data-stat="checked">0</span>
          </div>
          <div class="stat-card">
            <span class="stat-label">Cancelled</span>
            <span class="stat-value" data-stat="cancelled">0</span>
          </div>
        </div>

        <div style="display: flex; align-items: center; gap: 12px; margin-top: 16px; padding-top: 16px; border-top: 1px solid rgba(15, 23, 42, 0.06);">
          <span style="font-size: 13px; font-weight: 600; color: var(--muted);">View by:</span>
          <div class="filter-row" id="viewModeRow" style="margin-top: 0;">
            <button class="btn ghost filter-btn active" type="button" data-view="status">Status</button>
            <button class="btn ghost filter-btn" type="button" data-view="venue">Venue</button>
            <button class="btn ghost filter-btn" type="button" data-view="time">Time</button>
          </div>
        </div>

        <div id="sportTypeFilter" style="display: none; margin-top: 12px;">
          <span style="font-size: 13px; font-weight: 600; color: var(--muted);">Sport type:</span>
          <div class="filter-row" style="margin-top: 8px;">
            <button class="btn ghost filter-btn active" type="button" data-sport="all">All</button>
            <button class="btn ghost filter-btn" type="button" data-sport="basketball">Basketball</button>
            <button class="btn ghost filter-btn" type="button" data-sport="tabletennis">Table Tennis</button>
          </div>
        </div>
      </div>
    </section>

    <div class="list" id="bookingList"></div>
    <div class="list" id="groupedView" style="display: none;"></div>
    <div id="empty" class="muted" style="display:none;padding:12px;">No bookings found.</div>
  </main>

  <footer class="site-footer">
    <div class="site-footer__wrap">
      <span class="site-footer__text">&copy; 2026 Club Booking Portal</span>
      <div class="site-footer__actions">
        <button class="site-footer__btn info-trigger" type="button" data-info="privacy">Privacy</button>
        <button class="site-footer__btn info-trigger" type="button" data-info="terms">Terms</button>
        <button class="site-footer__btn info-trigger" type="button" data-info="help">Help</button>
      </div>
    </div>
  </footer>

  <div id="infoOverlay" class="info-overlay" aria-hidden="true">
    <div class="info-card" role="dialog" aria-modal="true" aria-labelledby="infoTitle">
      <button class="info-close" type="button" aria-label="Close">×</button>
      <h3 id="infoTitle">Privacy</h3>
      <div id="infoBody" class="info-body"></div>
    </div>
  </div>

  <script>
    const STORAGE_KEY = 'clubBookings';
    const defaultBookings = [
      { id: 1, member: 'Alex Chen', session: 'Basketball · Court A', time: '2026-01-27 18:00-19:00', status: 'pending' },
      { id: 2, member: 'Mia Zhang', session: 'Table Tennis · Hall 1', time: '2026-01-27 19:00-20:00', status: 'approved' },
      { id: 3, member: 'Sam Lee', session: 'Basketball · Court B', time: '2026-01-28 10:00-11:00', status: 'checked' },
      { id: 4, member: 'Chris Wu', session: 'Table Tennis · Hall 2', time: '2026-01-28 12:00-13:00', status: 'cancelled' },
      { id: 5, member: 'Emma Davis', session: 'Basketball · Court C', time: '2026-01-27 15:00-16:00', status: 'approved' },
      { id: 6, member: 'John Smith', session: 'Table Tennis · Hall 2', time: '2026-01-27 16:00-17:00', status: 'approved' }
    ];

    const listEl = document.getElementById('bookingList');
    const groupedView = document.getElementById('groupedView');
    const emptyEl = document.getElementById('empty');
    const viewModeRow = document.getElementById('viewModeRow');
    const sportTypeFilter = document.getElementById('sportTypeFilter');
    const refreshBtn = document.getElementById('refreshBtn');

    let currentSportFilter = 'all';

    const loadBookings = () => {
      try {
        const stored = JSON.parse(localStorage.getItem(STORAGE_KEY) || 'null');
        if (Array.isArray(stored) && stored.length) return stored;
      } catch (e) { /* ignore */ }
      localStorage.setItem(STORAGE_KEY, JSON.stringify(defaultBookings));
      return defaultBookings.slice();
    };

    const saveBookings = (items) => {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
    };

    const statusLabel = (status) => ({
      pending: 'Pending',
      approved: 'Approved',
      checked: 'Checked-in',
      cancelled: 'Cancelled'
    }[status] || 'Pending');

    const renderStats = (items) => {
      const counts = { pending: 0, approved: 0, checked: 0, cancelled: 0 };
      items.forEach((item) => { if (counts[item.status] !== undefined) counts[item.status] += 1; });
      Object.keys(counts).forEach((key) => {
        const el = document.querySelector(\`[data-stat="\${key}"]\`);
        if (el) el.textContent = String(counts[key]);
      });
    };

    const renderList = (items, filter = 'all') => {
      if (!listEl || !emptyEl) return;
      listEl.innerHTML = '';
      const statusOrder = { pending: 0, approved: 1, checked: 2, cancelled: 3 };
      const filtered = (filter === 'all' ? items : items.filter((item) => item.status === filter))
        .slice()
        .sort((a, b) => (statusOrder[a.status] ?? 99) - (statusOrder[b.status] ?? 99));
      emptyEl.style.display = filtered.length ? 'none' : 'block';
      filtered.forEach((item) => {
        const card = document.createElement('div');
        card.className = 'card booking-card';
        card.dataset.id = String(item.id);
        card.innerHTML = \`
          <div class="booking-meta">
            <div class="booking-title">\${item.member}</div>
            <div class="booking-sub">\${item.session}</div>
            <div class="booking-sub">\${item.time}</div>
          </div>
          <div class="booking-meta">
            <span class="status \${item.status}">\${statusLabel(item.status)}</span>
          </div>
          <div class="booking-actions">
            <button class="btn approve small" data-action="approve">Approve</button>
            <button class="btn checkin small" data-action="checkin">Check-in</button>
            <button class="btn cancel small" data-action="cancel">Cancel</button>
          </div>
        \`;
        listEl.appendChild(card);
      });
    };

    const renderGroupedView = (items, groupBy) => {
      if (!groupedView) return;
      groupedView.innerHTML = '';
      
      // Filter by sport type if viewing by venue
      let filteredItems = items;
      if (groupBy === 'venue') {
        filteredItems = items.filter(item => {
          if (currentSportFilter === 'all') return true;
          if (currentSportFilter === 'basketball') return item.session.includes('Basketball');
          if (currentSportFilter === 'tabletennis') return item.session.includes('Table Tennis');
          return true;
        });
      }
      
      let groups = {};
      
      if (groupBy === 'venue') {
        filteredItems.forEach(item => {
          const venue = item.session.split(' · ')[1] || 'Unknown';
          if (!groups[venue]) groups[venue] = [];
          groups[venue].push(item);
        });
        
        // Sort venues: Basketball courts (A, B, C, ...) first, then Table Tennis halls (1, 2, 3, ...)
        const venueOrder = (a, b) => {
          const isCourtA = /^Court/.test(a);
          const isCourtB = /^Court/.test(b);
          
          if (isCourtA && isCourtB) {
            // Both courts: sort alphabetically (A < B < C)
            return a.localeCompare(b);
          } else if (!isCourtA && !isCourtB) {
            // Both halls: sort numerically
            const numA = parseInt(a.match(/\\d+/)?.[0] || 0);
            const numB = parseInt(b.match(/\\d+/)?.[0] || 0);
            return numA - numB;
          }
          // Courts come before Halls
          return isCourtA ? -1 : 1;
        };
        
        Object.keys(groups).sort(venueOrder).forEach(groupName => {
          const section = document.createElement('div');
          section.className = 'group-section';
          
          const header = document.createElement('div');
          header.className = 'group-header';
          header.textContent = \`Venue: \${groupName}\`;
          section.appendChild(header);
          
          const groupList = document.createElement('div');
          groupList.className = 'group-list';
          
          groups[groupName].forEach(item => {
            const card = document.createElement('div');
            card.className = 'card booking-card';
            card.dataset.id = String(item.id);
            card.innerHTML = \`
              <div class="booking-meta">
                <div class="booking-title">\${item.member}</div>
                <div class="booking-sub">\${item.session}</div>
                <div class="booking-sub">\${item.time}</div>
              </div>
              <div class="booking-meta">
                <span class="status \${item.status}">\${statusLabel(item.status)}</span>
              </div>
              <div class="booking-actions">
                <button class="btn approve small" data-action="approve">Approve</button>
                <button class="btn checkin small" data-action="checkin">Check-in</button>
                <button class="btn cancel small" data-action="cancel">Cancel</button>
              </div>
            \`;
            groupList.appendChild(card);
          });
          
          section.appendChild(groupList);
          groupedView.appendChild(section);
        });
      } else if (groupBy === 'time') {
        items.forEach(item => {
          const date = item.time.split(' ')[0];
          if (!groups[date]) groups[date] = [];
          groups[date].push(item);
        });
        
        Object.keys(groups).sort().forEach(groupName => {
          const section = document.createElement('div');
          section.className = 'group-section';
          
          const header = document.createElement('div');
          header.className = 'group-header';
          header.textContent = \`Date: \${groupName}\`;
          section.appendChild(header);
          
          const groupList = document.createElement('div');
          groupList.className = 'group-list';
          
          groups[groupName].forEach(item => {
            const card = document.createElement('div');
            card.className = 'card booking-card';
            card.dataset.id = String(item.id);
            card.innerHTML = \`
              <div class="booking-meta">
                <div class="booking-title">\${item.member}</div>
                <div class="booking-sub">\${item.session}</div>
                <div class="booking-sub">\${item.time}</div>
              </div>
              <div class="booking-meta">
                <span class="status \${item.status}">\${statusLabel(item.status)}</span>
              </div>
              <div class="booking-actions">
                <button class="btn approve small" data-action="approve">Approve</button>
                <button class="btn checkin small" data-action="checkin">Check-in</button>
                <button class="btn cancel small" data-action="cancel">Cancel</button>
              </div>
            \`;
            groupList.appendChild(card);
          });
          
          section.appendChild(groupList);
          groupedView.appendChild(section);
        });
      }
    };

    let bookings = loadBookings();
    let viewMode = 'status';
    renderStats(bookings);
    renderList(bookings, 'all');

    refreshBtn?.addEventListener('click', () => {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(defaultBookings));
      bookings = defaultBookings.slice();
      renderStats(bookings);
      if (viewMode === 'status') {
        renderList(bookings, 'all');
      } else {
        renderGroupedView(bookings, viewMode);
      }
    });

    viewModeRow?.addEventListener('click', (event) => {
      const btn = event.target.closest('.filter-btn');
      if (!btn) return;
      viewMode = btn.dataset.view || 'status';
      viewModeRow.querySelectorAll('.filter-btn').forEach((el) => el.classList.toggle('active', el === btn));
      
      // Show/hide sport type filter based on view mode
      sportTypeFilter.style.display = viewMode === 'venue' ? 'block' : 'none';
      
      if (viewMode === 'status') {
        listEl.style.display = 'grid';
        groupedView.style.display = 'none';
        renderList(bookings, 'all');
      } else {
        listEl.style.display = 'none';
        groupedView.style.display = 'block';
        renderGroupedView(bookings, viewMode);
      }
    });

    // Sport type filter for venue view
    document.getElementById('sportTypeFilter')?.addEventListener('click', (event) => {
      const btn = event.target.closest('.filter-btn');
      if (!btn) return;
      currentSportFilter = btn.dataset.sport || 'all';
      document.querySelectorAll('#sportTypeFilter .filter-btn').forEach((el) => {
        el.classList.toggle('active', el === btn);
      });
      renderGroupedView(bookings, 'venue');
    });

    listEl?.addEventListener('click', (event) => {
      const btn = event.target.closest('button[data-action]');
      const card = event.target.closest('.booking-card');
      if (!btn || !card) return;
      const id = Number(card.dataset.id);
      const action = btn.dataset.action;
      const item = bookings.find((b) => b.id === id);
      if (!item) return;
      if (action === 'approve') item.status = 'approved';
      if (action === 'checkin') item.status = 'checked';
      if (action === 'cancel') item.status = 'cancelled';
      saveBookings(bookings);
      renderStats(bookings);
      if (viewMode === 'status') {
        renderList(bookings, 'all');
      } else {
        renderGroupedView(bookings, viewMode);
      }
    });

    groupedView?.addEventListener('click', (event) => {
      const btn = event.target.closest('button[data-action]');
      const card = event.target.closest('.booking-card');
      if (!btn || !card) return;
      const id = Number(card.dataset.id);
      const action = btn.dataset.action;
      const item = bookings.find((b) => b.id === id);
      if (!item) return;
      if (action === 'approve') item.status = 'approved';
      else if (action === 'checkin') item.status = 'checked';
      else if (action === 'cancel') item.status = 'cancelled';
      saveBookings(bookings);
      renderStats(bookings);
      renderGroupedView(bookings, viewMode);
    });

    refreshBtn?.addEventListener('click', () => {
      bookings = loadBookings();
      renderStats(bookings);
      renderList(bookings, activeFilter);
    });

    document.getElementById('logoutBtn')?.addEventListener('click', () => {
      localStorage.removeItem('loggedUser');
      localStorage.removeItem('user');
      window.location.href = 'login.html';
    });

    const infoOverlay = document.getElementById('infoOverlay');
    const infoTitle = document.getElementById('infoTitle');
    const infoBody = document.getElementById('infoBody');
    const infoMap = {
      privacy: {
        title: 'Privacy',
        body: \`
          <p>We only store the information needed to manage bookings and memberships.</p>
          <p>Your account data is kept locally in this demo and is not shared with third parties.</p>
          <p>You can request deletion at any time by contacting the admin.</p>
        \`
      },
      terms: {
        title: 'Terms',
        body: \`
          <p>Bookings are first-come, first-served and subject to club capacity.</p>
          <p>Members must follow club rules and respect facility policies.</p>
          <p>Repeated no-shows may result in booking restrictions.</p>
        \`
      },
      help: {
        title: 'Help',
        body: \`
          <p>Review pending requests daily to keep schedules accurate.</p>
          <p>Use Check-in once the member arrives on site.</p>
          <p>Contact support at support@example.com for further help.</p>
        \`
      }
    };

    const openInfo = (key) => {
      const data = infoMap[key];
      if (!data || !infoOverlay) return;
      if (infoTitle) infoTitle.textContent = data.title;
      if (infoBody) infoBody.innerHTML = data.body;
      infoOverlay.classList.add('open');
      document.body.classList.add('no-scroll');
    };

    const closeInfo = () => {
      if (!infoOverlay) return;
      infoOverlay.classList.remove('open');
      document.body.classList.remove('no-scroll');
    };

    document.querySelectorAll('.info-trigger').forEach((btn) => {
      btn.addEventListener('click', () => openInfo(btn.dataset.info));
    });
    infoOverlay?.addEventListener('click', (evt) => { if (evt.target === infoOverlay) closeInfo(); });
    infoOverlay?.querySelector('.info-close')?.addEventListener('click', closeInfo);
  <\/script>
</body>
</html>
`,Tc=`<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <title>Club Booking Portal - Club Home</title>
  <link rel="stylesheet" href="theme.css">
  <style>
    body {
      margin: 0;
    }
    .wrap {
      max-width: 1100px;
      margin: 0 auto;
      padding: 16px;
    }
    header {
      position: sticky;
      top: 0;
      z-index: 10;
      border-bottom: 1px solid var(--line);
    }
    .header-wrap {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
    }
    .header-actions {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
    }
    .site-title {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 0;
    }
    .site-title-link {
      color: inherit;
      text-decoration: none;
    }
    .btn {
      padding: 8px 16px;
      border-radius: 999px;
      font-size: 13px;
      cursor: pointer;
      text-decoration: none;
      display: inline-flex;
      align-items: center;
      justify-content: center;
    }
    .page-head {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
      margin: 16px 0 12px;
      flex-wrap: wrap;
    }
    .page-head h2 {
      margin: 0;
    }
    .page-head p {
      margin: 4px 0 0;
    }
    .page-actions {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
    }
    .card .pad {
      padding: 16px;
    }
    .stat-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
      gap: 12px;
    }
    .stat-card {
      background: #fff;
      border: 1px solid var(--line);
      border-radius: 12px;
      padding: 12px;
      display: grid;
      gap: 8px;
    }
    .stat-label {
      font-size: 12px;
      color: var(--muted);
    }
    .stat-value {
      font-size: 20px;
      font-weight: 700;
      color: var(--ink);
    }
    .stat-bar {
      height: 6px;
      background: var(--chip);
      border-radius: 999px;
      overflow: hidden;
    }
    .stat-bar span {
      display: block;
      height: 100%;
      width: 0%;
      background: linear-gradient(135deg, var(--accent), var(--accent-strong));
      border-radius: 999px;
      transition: width .4s var(--anim-base);
    }
    .section-head {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-top: 16px;
    }
    .section-head h3 {
      margin: 0;
    }
    .section-head p {
      margin: 4px 0 0;
    }
    .action-grid {
      margin-top: 12px;
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 16px;
    }
    .action-card {
      padding: 18px;
      min-height: 120px;
      display: grid;
      gap: 8px;
      text-decoration: none;
      color: inherit;
    }
    .action-card:hover {
      color: inherit;
    }
    .action-tag {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      padding: 4px 10px;
      border-radius: 999px;
      font-size: 11px;
      font-weight: 600;
      color: var(--accent-strong);
      background: rgba(47, 93, 255, 0.1);
      width: fit-content;
    }
    .action-card.disabled {
      opacity: 0.55;
      border-style: dashed;
      pointer-events: none;
    }
    .info-overlay {
      position: fixed;
      inset: 0;
      display: none;
      align-items: center;
      justify-content: center;
      padding: 24px;
      background: rgba(10, 12, 16, 0.45);
      z-index: 3000;
    }
    .info-overlay.open {
      display: flex;
    }
    .info-card {
      position: relative;
      width: min(560px, 92vw);
      background: #fff;
      border-radius: 16px;
      box-shadow: none;
      padding: 20px 20px 18px;
    }
    .info-card h3 {
      margin: 0 0 8px;
      font-size: 18px;
    }
    .info-body p {
      margin: 0 0 10px;
      color: var(--muted);
      font-size: 13px;
    }
    .info-close {
      position: absolute;
      top: 10px;
      right: 10px;
      width: 30px;
      height: 30px;
      border-radius: 999px;
      border: 1px solid var(--line);
      background: #fff;
      cursor: pointer;
      color: var(--muted);
    }
  </style>
</head>
<body>
  <header id="top">
    <div class="wrap header-wrap">
      <h1 class="site-title">Club Home</h1>
      <div class="header-actions">
        <button id="logoutBtn" class="btn" type="button" style="min-width:80px">Logout</button>
      </div>
    </div>
  </header>

  <main class="wrap">
    <div class="page-head">
      <div>
        <h2>Today's overview</h2>
        <p class="muted">Quick snapshot of bookings and attendance.</p>
      </div>
      <!-- <div class="page-actions">
        <a class="btn" href="club bookings.html">Review bookings</a>
        <a class="btn ghost" href="venue overview.html">Manage venues</a>
      </div> -->
    </div>

    <section class="card">
      <div class="pad">
        <div class="stat-grid" id="overviewGrid">
          <div class="stat-card" data-key="bookings" data-value="24">
            <span class="stat-label">Bookings</span>
            <span class="stat-value">24</span>
            <div class="stat-bar"><span></span></div>
          </div>
          <div class="stat-card" data-key="checkins" data-value="18">
            <span class="stat-label">Check-ins</span>
            <span class="stat-value">18</span>
            <div class="stat-bar"><span></span></div>
          </div>
          <div class="stat-card" data-key="cancellations" data-value="2">
            <span class="stat-label">Cancellations</span>
            <span class="stat-value">2</span>
            <div class="stat-bar"><span></span></div>
          </div>
          <div class="stat-card" data-key="courtsInUse" data-value="4" data-total="6">
            <span class="stat-label">Courts in use</span>
            <span class="stat-value">4 / 6</span>
            <div class="stat-bar"><span></span></div>
          </div>
        </div>
      </div>
    </section>

    <div class="section-head">
      <div>
        <h3>Quick actions</h3>
        <p class="muted">Jump back into key areas of your club.</p>
      </div>
    </div>
    <section class="action-grid">
      <a class="card action-card" href="onboarding.html">
        <h3>Club profile</h3>
        <p class="muted">Manage club info and location.</p>
        <span class="action-tag">Manage</span>
      </a>
      <a class="card action-card" href="club.html" target="_blank" rel="noopener">
        <h3>Public preview</h3>
        <p class="muted">See the club page as members do.</p>
        <span class="action-tag">Preview</span>
      </a>
      <a class="card action-card" href="venue overview.html">
        <h3>Venue overview</h3>
        <p class="muted">Manage courts, capacity, and slots.</p>
        <span class="action-tag">Manage</span>
      </a>
      <a class="card action-card" href="club bookings.html">
        <h3>Member bookings</h3>
        <p class="muted">Approve and manage reservations.</p>
        <span class="action-tag">Review</span>
      </a>
      <a class="card action-card" href="club updates.html">
        <h3>Member updates</h3>
        <p class="muted">Share updates with members.</p>
        <span class="action-tag">Manage</span>
      </a>
    </section>
  </main>
  <footer class="site-footer">
    <div class="site-footer__wrap">
      <span class="site-footer__text">&copy; 2026 Club Booking Portal</span>
      <div class="site-footer__actions">
        <button class="site-footer__btn info-trigger" type="button" data-info="privacy">Privacy</button>
        <button class="site-footer__btn info-trigger" type="button" data-info="terms">Terms</button>
        <button class="site-footer__btn info-trigger" type="button" data-info="help">Help</button>
      </div>
    </div>
  </footer>

  <div id="infoOverlay" class="info-overlay" aria-hidden="true">
    <div class="info-card" role="dialog" aria-modal="true" aria-labelledby="infoTitle">
      <button class="info-close" type="button" aria-label="Close">x</button>
      <h3 id="infoTitle">Privacy</h3>
      <div id="infoBody" class="info-body"></div>
    </div>
  </div>
  <script>
    document.getElementById('logoutBtn')?.addEventListener('click', () => {
      localStorage.removeItem('loggedUser');
      localStorage.removeItem('user');
      window.location.href = 'login.html';
    });

    const applyOverviewStats = (stats) => {
      const grid = document.getElementById('overviewGrid');
      if (!grid) return;
      const cards = grid.querySelectorAll('.stat-card');
      const bookings = stats?.bookings ?? Number(cards[0]?.dataset.value || 0);
      const checkins = stats?.checkins ?? Number(cards[1]?.dataset.value || 0);
      const cancellations = stats?.cancellations ?? Number(cards[2]?.dataset.value || 0);
      const courtsInUse = stats?.courtsInUse ?? Number(cards[3]?.dataset.value || 0);
      const courtsTotal = stats?.courtsTotal ?? Number(cards[3]?.dataset.total || 0);
      const maxValue = Math.max(bookings, checkins, cancellations, 1);

      const setCard = (key, value, total) => {
        const card = grid.querySelector(\`.stat-card[data-key="\${key}"]\`);
        if (!card) return;
        const valueEl = card.querySelector('.stat-value');
        const barEl = card.querySelector('.stat-bar span');
        if (valueEl) {
          valueEl.textContent = total ? \`\${value} / \${total}\` : \`\${value}\`;
        }
        if (barEl) {
          const pct = total ? (total > 0 ? (value / total) * 100 : 0) : (value / maxValue) * 100;
          barEl.style.width = \`\${Math.min(100, Math.max(0, pct))}%\`;
        }
      };

      setCard('bookings', bookings);
      setCard('checkins', checkins);
      setCard('cancellations', cancellations);
      setCard('courtsInUse', courtsInUse, courtsTotal);
    };

    // TODO: Replace with backend data fetch; expected shape:
    // { bookings: number, checkins: number, cancellations: number, courtsInUse: number, courtsTotal: number }
    if (window.clubStats) {
      applyOverviewStats(window.clubStats);
    } else {
      applyOverviewStats();
    }

    const infoOverlay = document.getElementById('infoOverlay');
    const infoTitle = document.getElementById('infoTitle');
    const infoBody = document.getElementById('infoBody');
    const infoMap = {
      privacy: {
        title: 'Privacy',
        body: \`
          <p>We only store the information needed to manage bookings and memberships.</p>
          <p>Your account data is kept locally in this demo and is not shared with third parties.</p>
          <p>You can request deletion at any time by contacting the admin.</p>
        \`
      },
      terms: {
        title: 'Terms',
        body: \`
          <p>Bookings are first-come, first-served and subject to club capacity.</p>
          <p>Members must follow club rules and respect facility policies.</p>
          <p>Repeated no-shows may result in booking restrictions.</p>
        \`
      },
      help: {
        title: 'Help',
        body: \`
          <p>Need assistance? Start by searching for a club and selecting a time slot.</p>
          <p>If you cannot log in, double-check your email and password.</p>
          <p>Contact support at support@example.com for further help.</p>
        \`
      }
    };

    const openInfo = (key) => {
      const data = infoMap[key];
      if (!data || !infoOverlay) return;
      if (infoTitle) infoTitle.textContent = data.title;
      if (infoBody) infoBody.innerHTML = data.body;
      infoOverlay.classList.add('open');
      document.body.classList.add('no-scroll');
    };

    const closeInfo = () => {
      if (!infoOverlay) return;
      infoOverlay.classList.remove('open');
      document.body.classList.remove('no-scroll');
    };

    document.querySelectorAll('.info-trigger').forEach((btn) => {
      btn.addEventListener('click', () => openInfo(btn.dataset.info));
    });
    infoOverlay?.addEventListener('click', (evt) => { if (evt.target === infoOverlay) closeInfo(); });
    infoOverlay?.querySelector('.info-close')?.addEventListener('click', closeInfo);
  <\/script>
</body>
</html>
`,Lc=`<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width,initial-scale=1" />
  <title>Club Registration</title>
  <link rel="stylesheet" href="theme.css">
  <style>
    body { margin: 0; }
    .club-register-page {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      background: linear-gradient(160deg, #f6f7fb 0%, #eef1f6 40%, #e9eef6 100%);
    }
    .page-back {
      position: fixed;
      top: 20px;
      left: 24px;
      z-index: 10;
    }
    .club-register {
      flex: 1;
      display: grid;
      grid-template-columns: minmax(280px, 1.2fr) minmax(300px, 0.8fr);
      gap: 0;
    }
    .club-hero {
      position: relative;
      padding: clamp(36px, 6vw, 80px);
      color: #0f172a;
      overflow: hidden;
    }
    .club-hero::before {
      content: "";
      position: absolute;
      inset: -40% 30% auto -30%;
      width: 520px;
      height: 520px;
      background: radial-gradient(circle at 30% 30%, rgba(47, 93, 255, 0.2), rgba(47, 93, 255, 0));
      opacity: 0.9;
      pointer-events: none;
    }
    .club-hero::after {
      content: "";
      position: absolute;
      bottom: -30%;
      right: -10%;
      width: 420px;
      height: 420px;
      background: radial-gradient(circle at 50% 50%, rgba(249, 115, 22, 0.18), rgba(249, 115, 22, 0));
      pointer-events: none;
    }
    .hero-inner {
      position: relative;
      max-width: 520px;
      z-index: 1;
    }
    .hero-badge {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      padding: 6px 14px;
      border-radius: 999px;
      background: rgba(255, 255, 255, 0.85);
      border: 1px solid rgba(15, 23, 42, 0.08);
      font-size: 12px;
      font-weight: 600;
      letter-spacing: 0.04em;
      text-transform: uppercase;
    }
    .hero-title {
      margin: 18px 0 12px;
      font-size: clamp(32px, 4vw, 46px);
    }
    .hero-lead {
      margin: 0 0 22px;
      color: #475569;
      font-size: 15px;
      line-height: 1.6;
    }
    .hero-list {
      list-style: none;
      padding: 0;
      margin: 0;
      display: grid;
      gap: 12px;
    }
    .hero-list li {
      background: rgba(255, 255, 255, 0.7);
      border: 1px solid rgba(15, 23, 42, 0.08);
      border-radius: 14px;
      padding: 12px 14px;
      font-size: 13px;
      color: #334155;
      box-shadow: none;
    }
    .club-panel {
      background: #fff;
      border-left: 1px solid rgba(15, 23, 42, 0.08);
      display: flex;
      align-items: center;
      justify-content: center;
      padding: clamp(28px, 4vw, 56px) clamp(24px, 4vw, 60px);
    }
    .form-shell {
      width: min(420px, 100%);
      padding: 0;
    }
    .top-link {
      display: flex;
      justify-content: flex-end;
      margin-bottom: 14px;
    }
    .form-title {
      margin: 0 0 4px;
      font-size: 28px;
    }
    .form-lead {
      margin: 0 0 16px;
      color: #6b7280;
      font-size: 13px;
    }
    .mb-3 { margin-bottom: 12px; }
    .btn-row { display: flex; gap: 12px; margin-top: 8px; }
    .form-btn {
      flex: 1;
      height: 42px;
      border-radius: 12px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      text-decoration: none;
      border: none;
      background: #2563eb;
      color: #fff;
      font-weight: 600;
      cursor: pointer;
      font-size: 14px;
      transition: transform 0.15s ease;
      box-shadow: none !important;
      outline: none;
    }
    .form-btn:hover {
      transform: translateY(-1px);
      box-shadow: none !important;
    }
    .form-btn:active {
      transform: translateY(0);
      box-shadow: none !important;
    }
    .form-btn:focus,
    .form-btn:focus-visible {
      outline: none;
      box-shadow: none !important;
    }
    .error {
      font-size: 12px;
      color: #dc2626;
      margin-top: 4px;
      display: none;
    }
    .status {
      display: none;
      padding: 10px 12px;
      border-radius: 12px;
      border: 1px solid rgba(47, 93, 255, 0.25);
      background: rgba(47, 93, 255, 0.08);
      color: #1f3bb3;
      font-size: 13px;
      margin-bottom: 12px;
    }
    .status.is-error {
      border-color: rgba(239, 68, 68, 0.4);
      background: rgba(239, 68, 68, 0.08);
      color: #b91c1c;
    }
    .info-overlay {
      position: fixed;
      inset: 0;
      display: none;
      align-items: center;
      justify-content: center;
      padding: 24px;
      background: rgba(10, 12, 16, 0.45);
      z-index: 3000;
    }
    .info-overlay.open {
      display: flex;
    }
    .info-card {
      position: relative;
      width: min(560px, 92vw);
      background: #fff;
      border-radius: 16px;
      box-shadow: none;
      padding: 20px 20px 18px;
    }
    .info-card h3 {
      margin: 0 0 8px;
      font-size: 18px;
    }
    .info-body p {
      margin: 0 0 10px;
      color: var(--muted);
      font-size: 13px;
    }
    .info-close {
      position: absolute;
      top: 10px;
      right: 10px;
      width: 30px;
      height: 30px;
      border-radius: 999px;
      border: 1px solid var(--line);
      background: #fff;
      cursor: pointer;
      color: var(--muted);
    }
    label {
      display: block;
      margin-bottom: 6px;
      font-weight: 600;
      color: #0f172a;
      font-size: 14px;
    }
    input[type="email"],
    input[type="password"],
    input[type="text"] {
      width: 100%;
      padding: 10px 12px;
      border: 1px solid rgba(15, 23, 42, 0.1);
      border-radius: 10px;
      background: #fff;
      font-size: 14px;
      color: #0f172a;
      box-shadow: none !important;
      transition: border-color 0.2s ease;
    }
    input[type="email"]:focus,
    input[type="password"]:focus,
    input[type="text"]:focus {
      outline: none;
      border-color: #2563eb;
      box-shadow: none !important;
    }
    input:-webkit-autofill {
      -webkit-box-shadow: 0 0 0 30px #fff inset !important;
      box-shadow: 0 0 0 30px #fff inset !important;
    }
    @media (max-width: 900px) {
      .club-register { grid-template-columns: 1fr; }
      .club-panel { border-left: none; border-top: 1px solid rgba(15, 23, 42, 0.08); }
      .hero-inner { max-width: none; }
    }
  </style>
</head>
<body>
  <div class="club-register-page">
    <button id="pageBack" class="link-back page-back" type="button">Back</button>
    <main class="club-register">
      <section class="club-hero">
        <div class="hero-inner">
          <span class="hero-badge">Club Account</span>
          <h1 class="hero-title">Start your club registration</h1>
          <p class="hero-lead">Create a club profile to manage schedules, accept bookings, and share updates with members.</p>
          <ul class="hero-list">
            <li>Set availability and manage court schedules in minutes.</li>
            <li>Highlight your club types so members find you faster.</li>
            <li>Keep bookings organized with one shared dashboard.</li>
          </ul>
        </div>
      </section>

      <section class="club-panel">
        <div class="form-shell">
          <h2 class="form-title">Club registration</h2>
          <p class="form-lead">Fill in the details below to create your club account.</p>

          <div id="statusMsg" class="status" role="status" aria-live="polite"></div>

          <div class="mb-3">
            <label for="clubEmail">Email</label>
            <input id="clubEmail" type="email" placeholder="Enter your email" autocomplete="email" />
            <div id="clubEmailErr" class="error">Please enter a valid email address.</div>
          </div>

          <div class="mb-3">
            <label for="clubPass">Password</label>
            <input id="clubPass" type="password" placeholder="Enter your password" autocomplete="new-password" />
            <div id="clubPassErr" class="error">Password is required.</div>
          </div>

          <div class="mb-3">
            <label for="clubPass2">Confirm password</label>
            <input id="clubPass2" type="password" placeholder="Re-enter your password" autocomplete="new-password" />
            <div id="clubPass2Err" class="error">Passwords do not match.</div>
          </div>

          <div class="btn-row">
            <button id="clubRegisterBtn" class="btn form-btn" type="button">Next</button>
          </div>
        </div>
      </section>
    </main>

    <footer class="site-footer">
      <div class="site-footer__wrap">
        <span class="site-footer__text">&copy; 2026 Club Booking Portal</span>
        <div class="site-footer__actions">
          <button class="site-footer__btn info-trigger" type="button" data-info="privacy">Privacy</button>
          <button class="site-footer__btn info-trigger" type="button" data-info="terms">Terms</button>
          <button class="site-footer__btn info-trigger" type="button" data-info="help">Help</button>
        </div>
      </div>
    </footer>
  </div>

  <div id="infoOverlay" class="info-overlay" aria-hidden="true">
    <div class="info-card" role="dialog" aria-modal="true" aria-labelledby="infoTitle">
      <button class="info-close" type="button" aria-label="Close">x</button>
      <h3 id="infoTitle">Privacy</h3>
      <div id="infoBody" class="info-body"></div>
    </div>
  </div>

  <script>
    const API_BASE = "http://13.40.74.21:8080/api";
    // TODO: Replace with real backend endpoint for club registration.
    const CLUB_REGISTER_ENDPOINT = \`\${API_BASE}/clubs/register\`;
    const emailRe = /^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$/;

    const pageBack = document.getElementById('pageBack');
    const infoOverlay = document.getElementById('infoOverlay');
    const infoTitle = document.getElementById('infoTitle');
    const infoBody = document.getElementById('infoBody');
    const statusMsg = document.getElementById('statusMsg');
    const clubEmail = document.getElementById('clubEmail');
    const clubPass = document.getElementById('clubPass');
    const clubPass2 = document.getElementById('clubPass2');

    const clubEmailErr = document.getElementById('clubEmailErr');
    const clubPassErr = document.getElementById('clubPassErr');
    const clubPass2Err = document.getElementById('clubPass2Err');

    const showStatus = (text, isError) => {
      if (!statusMsg) return;
      statusMsg.textContent = text;
      statusMsg.classList.toggle('is-error', !!isError);
      statusMsg.style.display = 'block';
    };

    const clearStatus = () => {
      if (!statusMsg) return;
      statusMsg.style.display = 'none';
      statusMsg.classList.remove('is-error');
      statusMsg.textContent = '';
    };

    const setErr = (el, show) => {
      if (!el) return;
      el.style.display = show ? 'block' : 'none';
    };

    const resolveUserType = (role) => {
      const r = String(role || '').toLowerCase();
      if (r.includes('club') || r.includes('admin') || r.includes('leader')) return 'club';
      return 'user';
    };

    document.getElementById('clubRegisterBtn')?.addEventListener('click', async () => {
      clearStatus();
      let ok = true;
      const email = clubEmail.value.trim();
      const pass = clubPass.value;
      const pass2 = clubPass2.value;

      setErr(clubEmailErr, !emailRe.test(email));
      if (!emailRe.test(email)) ok = false;
      setErr(clubPassErr, !pass);
      if (!pass) ok = false;
      setErr(clubPass2Err, pass !== pass2);
      if (pass !== pass2) ok = false;

      if (!ok) return;

      const btn = document.getElementById('clubRegisterBtn');
      btn.disabled = true;
      btn.textContent = 'Submitting...';

      try {
        const fullName = email.split('@')[0];
        const res = await fetch(CLUB_REGISTER_ENDPOINT, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ fullName, email, password: pass })
        });

        if (res.ok) {
          let saved = null;
          try { saved = await res.json(); } catch (e) { saved = null; }
          const profileToSave = saved || { fullName, email, role: 'club' };
          const userType = resolveUserType(profileToSave.role || profileToSave.type);
          localStorage.setItem('user', JSON.stringify(profileToSave));
          localStorage.setItem('loggedUser', JSON.stringify({
            email: profileToSave.email || email,
            name: profileToSave.fullName || fullName,
            type: userType
          }));
          showStatus('Registered successfully! Redirecting to club home...', false);
          setTimeout(() => { window.location.href = 'club home.html'; }, 900);
        } else if (res.status === 409) {
          showStatus('Email already exists.', true);
        } else {
          const t = await res.text();
          showStatus(t || 'Registration failed. Please try again.', true);
        }
      } catch (err) {
        showStatus('Network error. Please try again.', true);
      } finally {
        btn.disabled = false;
        btn.textContent = 'Next';
      }
    });

    pageBack?.addEventListener('click', () => {
      if (window.history.length > 1) {
        window.history.back();
      } else {
        window.location.href = 'login.html#login';
      }
    });

    const infoMap = {
      privacy: {
        title: 'Privacy',
        body: \`
          <p>We only store the information needed to manage bookings and memberships.</p>
          <p>Your account data is kept locally in this demo and is not shared with third parties.</p>
          <p>You can request deletion at any time by contacting the admin.</p>
        \`
      },
      terms: {
        title: 'Terms',
        body: \`
          <p>Bookings are first-come, first-served and subject to club capacity.</p>
          <p>Members must follow club rules and respect facility policies.</p>
          <p>Repeated no-shows may result in booking restrictions.</p>
        \`
      },
      help: {
        title: 'Help',
        body: \`
          <p>Need assistance? Start by searching for a club and selecting a time slot.</p>
          <p>If you cannot log in, double-check your email and password.</p>
          <p>Contact support at support@example.com for further help.</p>
        \`
      }
    };

    const openInfo = (key) => {
      const data = infoMap[key];
      if (!data || !infoOverlay) return;
      if (infoTitle) infoTitle.textContent = data.title;
      if (infoBody) infoBody.innerHTML = data.body;
      infoOverlay.classList.add('open');
      document.body.classList.add('no-scroll');
    };

    const closeInfo = () => {
      if (!infoOverlay) return;
      infoOverlay.classList.remove('open');
      document.body.classList.remove('no-scroll');
    };

    document.querySelectorAll('.info-trigger').forEach((btn) => {
      btn.addEventListener('click', () => openInfo(btn.dataset.info));
    });
    infoOverlay?.addEventListener('click', (evt) => { if (evt.target === infoOverlay) closeInfo(); });
    infoOverlay?.querySelector('.info-close')?.addEventListener('click', closeInfo);
  <\/script>
</body>
</html>
`,Pc=`<!doctype html>\r
<html lang="en">\r
<head>\r
<meta charset="utf-8" />\r
<meta name="viewport" content="width=device-width,initial-scale=1" />\r
<title>Club Booking Portal · Club Page</title>\r
  <link rel="stylesheet" href="theme.css">\r
<style>\r
  :root {\r
    --bg: #f7f7f8;\r
    --card: #fff;\r
    --ink: #222;\r
    --muted: #6b7280;\r
    --line: #e5e7eb;\r
    --chip: #f3f4f6;\r
    --danger: #dc2626;\r
  }\r
\r
  * {\r
    box-sizing: border-box;\r
  }\r
\r
  body {\r
    margin: 0;\r
    background: linear-gradient(#fff, #fafafa);\r
    color: var(--ink);\r
    font: 14px/1.5 system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial;\r
  }\r
\r
  .preview-banner {\r
    background: #fef08a;\r
    border-bottom: 2px solid #eab308;\r
    padding: 12px 16px;\r
    text-align: center;\r
    font-weight: 600;\r
    color: #854d0e;\r
    font-size: 14px;\r
  }\r
\r
  header,\r
  footer {\r
    border-bottom: 1px solid var(--line);\r
    background: #fff;\r
    position: sticky;\r
    top: 0;\r
    z-index: 10;\r
  }\r
\r
  header .wrap,\r
  footer .wrap,\r
  .wrap {\r
    max-width: 1100px;\r
    margin: 0 auto;\r
    padding: 16px;\r
  }\r
\r
  a {\r
    color: inherit;\r
    text-decoration: none;\r
  }\r
\r
  h1 {\r
    font-size: 22px;\r
    margin: 0;\r
  }\r
\r
  h2 {\r
    font-size: 18px;\r
    margin: 16px 0 12px;\r
  }\r
\r
  h3 {\r
    font-size: 15px;\r
    margin: 8px 0;\r
  }\r
\r
  .btn {\r
    border: 1px solid var(--line);\r
    background: #111;\r
    color: #fff;\r
    padding: 8px 12px;\r
    border-radius: 8px;\r
    font-size: 13px;\r
    cursor: pointer;\r
  }\r
\r
  .btn.ghost {\r
    background: #fff;\r
    color: #111;\r
  }\r
\r
  .btn.blue {\r
    background: #2563eb;\r
    border-color: #2563eb;\r
  }\r
\r
  .user-menu {\r
    position: relative;\r
    display: inline-block;\r
  }\r
\r
  .user-menu .menu {\r
    position: absolute;\r
    top: calc(100% + 4px);\r
    right: 0;\r
    background: #fff;\r
    border: 1px solid var(--line);\r
    border-radius: 10px;\r
    box-shadow: 0 12px 28px rgba(15, 23, 42, .12);\r
    padding: 6px 0;\r
    display: none;\r
    min-width: 140px;\r
    z-index: 20;\r
  }\r
\r
  .user-menu .menu.open {\r
    display: block;\r
  }\r
\r
  .user-menu .menu button {\r
    width: 100%;\r
    background: none;\r
    border: none;\r
    padding: 8px 14px;\r
    text-align: left;\r
    font-size: 13px;\r
    color: var(--ink);\r
    cursor: pointer;\r
  }\r
\r
  .user-menu .menu button:hover {\r
    background: #f3f4f6;\r
  }\r
\r
  .btn.red {\r
    background: #b91c1c;\r
    border-color: #b91c1c;\r
  }\r
\r
  .grid {\r
    display: grid;\r
    gap: 16px;\r
  }\r
\r
  .card {\r
    background: var(--card);\r
    border: 1px solid var(--line);\r
    border-radius: 16px;\r
    box-shadow: 0 2px 8px rgba(0, 0, 0, .04);\r
  }\r
\r
  .card .pad {\r
    padding: 16px;\r
  }\r
\r
  .muted {\r
    color: var(--muted);\r
    font-size: 12px;\r
  }\r
\r
  .thumb {\r
    height: 180px;\r
    background: #f3f4f6;\r
    border: 1px dashed var(--line);\r
    border-radius: 12px;\r
    color: var(--muted);\r
    display: flex;\r
    align-items: center;\r
    justify-content: center;\r
  }\r
\r
  .chips {\r
    display: flex;\r
    gap: 8px;\r
    flex-wrap: wrap;\r
    align-items: center;\r
  }\r
  .chip {\r
    background: var(--chip);\r
    border: 1px solid var(--line);\r
    border-radius: 999px;\r
    padding: 4px 10px;\r
    font-size: 12px;\r
    display: inline-flex;\r
    align-items: center;\r
    gap: 6px;\r
  }\r
\r
  .chip-hours {\r
    gap: 10px;\r
    padding-right: 16px;\r
  }\r
\r
  .chip-divider {\r
    color: var(--line);\r
  }\r
.kpi {\r
    display: grid;\r
    grid-template-columns: repeat(4, 1fr);\r
    gap: 12px;\r
  }\r
\r
  .k {\r
    background: #f3f4f6;\r
    border: 1px solid var(--line);\r
    border-radius: 10px;\r
    padding: 8px;\r
  }\r
\r
  .k .num {\r
    font-size: 18px;\r
    font-weight: 700;\r
  }\r
\r
  .table {\r
    width: 100%;\r
    border-collapse: collapse;\r
    font-size: 13px;\r
  }\r
\r
  .table th,\r
  .table td {\r
    padding: 10px;\r
    border-top: 1px solid var(--line);\r
    text-align: left;\r
  }\r
\r
  .crumbs {\r
    font-size: 12px;\r
    color: var(--muted);\r
    display: flex;\r
    gap: 6px;\r
    align-items: center;\r
  }\r
\r
  .crumbs a {\r
    color: #2563eb;\r
  }\r
\r
  select {\r
    height: 36px;\r
    border: 1px solid var(--line);\r
    border-radius: 8px;\r
    padding: 0 10px;\r
    background: #fff;\r
  }\r
\r
  input[type="text"] {\r
    height: 36px;\r
    border: 1px solid var(--line);\r
    border-radius: 8px;\r
    padding: 0 10px;\r
  }\r
\r
  #schedule {\r
    overflow-x: auto;\r
  }\r
\r
  .schedule-grid {\r
    display: grid;\r
    gap: 0;\r
    border: 1px solid var(--line);\r
    border-radius: 12px;\r
    overflow: hidden;\r
  }\r
\r
  .schedule-grid .cell {\r
    padding: 10px 8px;\r
    border-top: 1px solid var(--line);\r
  }\r
\r
  .schedule-grid .head {\r
    background: #f9fafb;\r
    font-weight: 600;\r
  }\r
\r
  .schedule-grid .time {\r
    background: #f9fafb;\r
    border-right: 1px solid var(--line);\r
    color: var(--muted);\r
    font-size: 12px;\r
  }\r
\r
  .slot-ok {\r
    padding: 8px 10px;\r
    margin: 6px;\r
    border: 1px solid #bfdbfe;\r
    background: #dbeafe;\r
    border-radius: 10px;\r
    text-align: center;\r
    cursor: pointer;\r
    display: flex;\r
    flex-direction: column;\r
    align-items: center;\r
    justify-content: center;\r
    gap: 4px;\r
    min-height: 44px;\r
  }\r
\r
  .slot-full {\r
    padding: 8px 10px;\r
    margin: 6px;\r
    border: 1px solid #d1d5db;\r
    background: #e5e7eb;\r
    border-radius: 10px;\r
    text-align: center;\r
    cursor: pointer;\r
    display: flex;\r
    flex-direction: column;\r
    align-items: center;\r
    justify-content: center;\r
    gap: 4px;\r
    min-height: 44px;\r
  }\r
\r
  .slot-label {\r
    font-size: 12px;\r
    font-weight: 600;\r
  }\r
\r
  .slot-full .slot-label {\r
    color: #6b7280;\r
  }\r
\r
  .slot-price {\r
    font-size: 11px;\r
    font-weight: 600;\r
    color: #1d4ed8;\r
    background: #eff6ff;\r
    border: 1px solid #bfdbfe;\r
    border-radius: 999px;\r
    padding: 2px 8px;\r
  }\r
\r
  .pill {\r
    display: inline-flex;\r
    align-items: center;\r
    gap: 6px;\r
    background: #f3f4f6;\r
    border: 1px solid var(--line);\r
    padding: 6px 10px;\r
    border-radius: 999px;\r
    font-size: 12px;\r
  }\r
\r
  .pill button {\r
    border: none;\r
    background: transparent;\r
    color: #555;\r
    cursor: pointer;\r
  }\r
\r
  .info-overlay {\r
    position: fixed;\r
    inset: 0;\r
    display: none;\r
    align-items: center;\r
    justify-content: center;\r
    padding: 24px;\r
    background: rgba(10, 12, 16, 0.45);\r
    z-index: 3000;\r
  }\r
\r
  .info-overlay.open {\r
    display: flex;\r
  }\r
\r
  .info-card {\r
    position: relative;\r
    width: min(560px, 92vw);\r
    background: #fff;\r
    border-radius: 16px;\r
    box-shadow: 0 24px 60px rgba(10, 12, 16, 0.25);\r
    padding: 20px 20px 18px;\r
  }\r
\r
  .info-card h3 {\r
    margin: 0 0 8px;\r
    font-size: 18px;\r
  }\r
\r
  .info-body p {\r
    margin: 0 0 10px;\r
    color: var(--muted);\r
    font-size: 13px;\r
  }\r
\r
  .info-close {\r
    position: absolute;\r
    top: 10px;\r
    right: 10px;\r
    width: 30px;\r
    height: 30px;\r
    border-radius: 999px;\r
    border: 1px solid var(--line);\r
    background: #fff;\r
    cursor: pointer;\r
    color: var(--muted);\r
  }\r
</style>\r
</head>\r
<body>\r
  <!-- 预览状态提示：显示这是上架前的预览版本 -->\r
  <div class="preview-banner">\r
    ⚠️ Preview: This is a preview before changes go live\r
  </div>\r
\r
  <!-- 顶部导航栏：包含网站标题、返回首页链接和用户登录/注册入口 -->\r
  <header>\r
    <div class="wrap" style="display:flex;align-items:center;justify-content:space-between;gap:12px">\r
      <div style="display:flex;align-items:center;gap:16px">\r
        <button class="link-back" onclick="window.parent.location.href = '/club-home'" type="button" style="font-size:14px;color:#0066cc;text-decoration:none;border:none;background:none;cursor:pointer;padding:8px 0">← Back</button>\r
        <h2 style="margin:0;font-size:18px;font-weight:600">Public preview</h2>\r
      </div>\r
      <div style="display:flex;gap:8px;align-items:center">\r
        <button class="btn blue" type="button">Confirm</button>\r
      </div>\r
    </div>\r
  </header>\r
\r
  <div class="wrap">\r
    <div class="card"><div class="pad">\r
      <div class="thumb">Club cover / banner</div>\r
      <h2>Club 1</h2>\r
      <p class="muted">Suitable for all levels, regular evening training and friendlies.</p>\r
      <div class="chips">\r
        <span class="chip">&#x1F3F8; Badminton</span>\r
        <span class="chip">&#x1F4CD; Hall A</span>\r
        <span class="chip chip-hours">\r
          <span>&#x1F552; Opening hours <span id="hoursLabel">08:00 - 24:00</span></span>\r
          <span class="chip-divider">|</span>\r
          <span>Courts: 3</span>\r
        </span>\r
      </div>\r
    </div></div>\r
\r
\r
    <!-- 俱乐部概况卡片：展示封面、描述、标签和关键指标 (KPI) -->\r
    \r
\r
    <!-- 排程管理卡片：包含营业时间设置、场地管理和排程网格 -->\r
        <div class="card"><div class="pad">\r
      <h3>Booking board</h3>\r
      <p class="muted">Tap a slot to toggle between available and full (demo only).</p>\r
      <div id="schedule" style="margin-top:12px"></div>\r
    </div></div>\r
\r
    <!-- 成员列表卡片：展示俱乐部成员及管理操作 -->\r
<!-- 风险操作区：删除俱乐部或重置数据 -->\r
</div>\r
\r
  <!-- 页脚：包含版权信息和辅助链接 -->\r
  <footer>\r
    <div class="wrap" style="display:flex;justify-content:space-between;align-items:center;margin-top:24px">\r
      <span class="site-footer__text">© <span id="y"></span> Club Booking Portal</span>\r
      <div style="display:flex;gap:8px">\r
        <button class="btn ghost info-trigger" type="button" data-info="privacy">Privacy</button>\r
        <button class="btn ghost info-trigger" type="button" data-info="terms">Terms</button>\r
        <button class="btn ghost info-trigger" type="button" data-info="help">Help</button>\r
      </div>\r
    </div>\r
  </footer>\r
\r
  <!-- 信息弹窗：用于显示隐私政策、服务条款等静态内容 -->\r
  <div id="infoOverlay" class="info-overlay" aria-hidden="true">\r
    <div class="info-card" role="dialog" aria-modal="true" aria-labelledby="infoTitle">\r
      <button class="info-close" type="button" aria-label="Close">x</button>\r
      <h3 id="infoTitle">Privacy</h3>\r
      <div id="infoBody" class="info-body"></div>\r
    </div>\r
  </div>\r
\r
  <script>\r
    // 1. 用户会话管理\r
    // 负责检查本地存储中的登录状态，并根据用户类型（普通用户或俱乐部管理员）渲染顶部导航栏的用户区域。\r
    // 如果已登录，显示下拉菜单；如果未登录，显示登录/注册按钮。\r
    const userArea = document.getElementById('userArea');\r
    if (userArea) {\r
      let savedUser = null;\r
      try {\r
        savedUser = JSON.parse(localStorage.getItem('loggedUser') || 'null');\r
      } catch (err) {\r
        savedUser = null;\r
        localStorage.removeItem('loggedUser');\r
      }\r
\r
      if (savedUser) {\r
        // 已登录：渲染用户菜单（My/Club 按钮及下拉菜单）\r
        const isClub = (savedUser && savedUser.type) === 'club';\r
        const targetHref = isClub ? 'club.html' : 'user.html';\r
        const myLabel = isClub ? 'Club' : 'My';\r
        userArea.innerHTML = \`\r
          <div class="user-menu">\r
            <button id="btnMy" class="btn blue" type="button" aria-haspopup="true" aria-expanded="false">\${myLabel}</button>\r
            <div class="menu" id="myMenu" role="menu">\r
              <button type="button" id="btnDetails" role="menuitem">Details</button>\r
              <button type="button" id="btnLogout" role="menuitem">Logout</button>\r
            </div>\r
          </div>\r
        \`;\r
\r
        const myBtn = document.getElementById('btnMy');\r
        const menuEl = document.getElementById('myMenu');\r
        const detailsBtn = document.getElementById('btnDetails');\r
        const logoutBtn = document.getElementById('btnLogout');\r
\r
        // 绑定详情按钮事件\r
    if (detailsBtn) {\r
          detailsBtn.addEventListener('click', () => {\r
            window.location.href = targetHref;\r
          });\r
        }\r
\r
        // 绑定登出逻辑\r
        if (logoutBtn) {\r
          logoutBtn.addEventListener('click', () => {\r
            localStorage.removeItem('loggedUser');\r
            location.reload();\r
          });\r
        }\r
\r
        // 下拉菜单交互逻辑（点击、悬停、键盘支持）\r
        if (myBtn && menuEl) {\r
          let closeTimer = null;\r
          const wrapper = userArea ? userArea.querySelector('.user-menu') : null;\r
          const CLOSE_DELAY = 400;\r
\r
          const clearCloseTimer = () => {\r
            if (closeTimer) {\r
              clearTimeout(closeTimer);\r
              closeTimer = null;\r
            }\r
          };\r
\r
          const openMenu = () => {\r
            clearCloseTimer();\r
            menuEl.classList.add('open');\r
            myBtn.setAttribute('aria-expanded', 'true');\r
          };\r
\r
          const closeMenu = () => {\r
            clearCloseTimer();\r
            menuEl.classList.remove('open');\r
            myBtn.setAttribute('aria-expanded', 'false');\r
          };\r
\r
          const scheduleClose = (delay = CLOSE_DELAY) => {\r
            clearCloseTimer();\r
            closeTimer = window.setTimeout(() => {\r
              menuEl.classList.remove('open');\r
              myBtn.setAttribute('aria-expanded', 'false');\r
              closeTimer = null;\r
            }, delay);\r
          };\r
\r
          myBtn.addEventListener('click', (evt) => {\r
            evt.preventDefault();\r
            if (menuEl.classList.contains('open')) {\r
              closeMenu();\r
            } else {\r
              openMenu();\r
            }\r
          });\r
\r
          myBtn.addEventListener('focus', openMenu);\r
          myBtn.addEventListener('mouseenter', openMenu);\r
          myBtn.addEventListener('keydown', (evt) => {\r
            if (evt.key === 'Escape') {\r
              closeMenu();\r
              myBtn.blur();\r
            }\r
          });\r
\r
          menuEl.addEventListener('focusin', openMenu);\r
          menuEl.addEventListener('mouseenter', openMenu);\r
          menuEl.addEventListener('mouseleave', () => scheduleClose());\r
          menuEl.addEventListener('keydown', (evt) => {\r
            if (evt.key === 'Escape') {\r
              closeMenu();\r
              myBtn.focus();\r
            }\r
          });\r
\r
          if (wrapper) {\r
            wrapper.addEventListener('mouseenter', openMenu);\r
            wrapper.addEventListener('mouseleave', () => scheduleClose());\r
          }\r
\r
          document.addEventListener('click', (evt) => {\r
            if (wrapper && wrapper.contains(evt.target)) {\r
              return;\r
            }\r
            closeMenu();\r
          });\r
        }\r
      } else {\r
        // 未登录：绑定登录/注册按钮事件\r
        const loginBtn = document.getElementById('btnLogin');\r
        const registerBtn = document.getElementById('btnRegister');\r
        const openAuth = (mode) => {\r
          if (window.openAuthModal) return window.openAuthModal(mode);\r
          window.location.href = mode === 'register' ? 'login.html#register' : 'login.html#login';\r
        };\r
        if (loginBtn) loginBtn.addEventListener('click', () => openAuth('login'));\r
        if (registerBtn) registerBtn.addEventListener('click', () => openAuth('register'));\r
      }\r
    }\r
\r
    // 2. 演示数据与状态管理\r
    // 定义场地列表和初始排程数据（模拟后端数据）。\r
    // seed 对象用于生成确定性的伪随机初始状态，以便演示时看起来有数据。\r
    const courts=['Court A','Court B','Court C'];\r
    const scheduleData={};\r
    const seed={\r
      8:['ok','full','ok'],9:['ok','ok','ok'],10:['full','ok','ok'],11:['ok','ok','full'],\r
      12:['ok','ok','ok'],13:['ok','full','ok'],14:['ok','ok','ok'],15:['full','ok','ok'],\r
      16:['ok','ok','ok'],17:['ok','ok','full'],18:['ok','ok','ok'],19:['ok','full','ok'],\r
      20:['ok','ok','ok'],21:['ok','ok','ok'],22:['ok','ok','ok'],23:['ok','ok','ok']\r
    };\r
    // �\r
    function ensureHour(hour){\r
      if(!scheduleData[hour]) scheduleData[hour] = {};\r
      courts.forEach((c,idx)=>{\r
        if(!scheduleData[hour][c]){\r
          const arr = seed[hour];\r
          scheduleData[hour][c] = Array.isArray(arr) && idx < arr.length ? (arr[idx]==='full'?'full':'ok') : 'ok';\r
        }\r
      });\r
      // 移除已删除的场地\r
      Object.keys(scheduleData[hour]).forEach(c=>{ if(!courts.includes(c)) delete scheduleData[hour][c]; });\r
    }\r
\r
    // 3. DOM �\r
    const scheduleEl=document.getElementById('schedule');\r
    const openHour=document.getElementById('openHour');\r
    const closeHour=document.getElementById('closeHour');\r
    const hoursLabel=document.getElementById('hoursLabel');\r
      const courtNameEl=document.getElementById('courtName');\r
      const addCourtBtn=document.getElementById('addCourt');\r
      const courtListEl=document.getElementById('courtList');\r
\r
      function pad(n){return String(n).padStart(2,'0');}\r
      const slotPrice = (hour, court) => {\r
        const base = 7;\r
        const peak = hour >= 18 ? 2 : 0;\r
        const courtOffset = courts.indexOf(court) % 2;\r
        return base + peak + courtOffset;\r
      };\r
\r
    // 动态生成小时选择下拉框的选项\r
    // 用于填充开始时间和结束时间的选择器\r
\r
    function buildHourOptions(selectEl,defaultVal,max=24){\r
      if(!selectEl) return;\r
      selectEl.innerHTML='';\r
      for(let i=0;i<=max;i++){\r
        const opt=document.createElement('option');\r
        opt.value=i; opt.textContent=\`\${i}:00\`;\r
        if(i===defaultVal) opt.selected=true;\r
        selectEl.appendChild(opt);\r
      }\r
    }\r
\r
    // 4. 场地管理逻辑\r
    // 渲染已添加的场地标签（Pills），并绑定删除事件。\r
    // 当场地列表发生变化时，会触发排程表的重新渲染。\r
    function renderCourts(){\r
        if(!courtListEl) return;\r
      courtListEl.innerHTML='';\r
      courts.forEach((c,idx)=>{\r
        const pill=document.createElement('span');\r
        pill.className='pill';\r
        pill.innerHTML = \`\${c} <button title="Remove" aria-label="Remove \${c}" data-idx="\${idx}">×</button>\`;\r
        pill.querySelector('button').addEventListener('click',()=>{\r
          // 移除场地并重新渲染\r
          courts.splice(idx,1);\r
          renderCourts();\r
          renderSchedule(+openHour.value,+closeHour.value);\r
        });\r
        courtListEl.appendChild(pill);\r
      });\r
    }\r
\r
    // 5. 排程表渲染核心逻辑\r
    // 根据选定的营业时间范围（start 至 end），动态生成 CSS Grid 布局的排程表。\r
    // 表格包含表头（时间/场地）和时间槽单元格。\r
    function renderSchedule(start,end){\r
        if(end<=start){\r
          scheduleEl.innerHTML='<div class="muted" style="padding:12px">Closing hour must be later than opening hour.</div>';\r
          return;\r
        }\r
        if(!courts.length){\r
          scheduleEl.innerHTML='<div class="muted" style="padding:12px">Add at least one court or area to show the board.</div>';\r
          return;\r
        }\r
        hoursLabel.textContent=\`\${pad(start)}:00 - \${pad(end)}:00\`;\r
        for(let h=start;h<end;h++) ensureHour(h);\r
\r
        const cols = courts.length + 1;\r
        const template = \`repeat(\${cols}, minmax(120px, 1fr))\`;\r
        let html = \`<div class="schedule-grid" style="grid-template-columns:\${template}">\`;\r
        html += \`<div class="cell head time">Time</div>\`;\r
        courts.forEach((c)=>{ html += \`<div class="cell head">\${c}</div>\`; });\r
\r
        for(let h=start; h<end; h++){\r
          const next=h+1;\r
          html += \`<div class="cell time">\${pad(h)}:00 - \${pad(next)}:00</div>\`;\r
          courts.forEach((c)=>{\r
            const s = scheduleData[h]?.[c] || 'ok';\r
            const cls = s==='full' ? 'slot-full' : 'slot-ok';\r
            if(s === 'full'){\r
              html += \`<div class="cell"><div class="\${cls}" data-hour="\${h}" data-court="\${encodeURIComponent(c)}" title="Fully booked"><div class="slot-label">Fully booked</div></div></div>\`;\r
              return;\r
            }\r
            const price = slotPrice(h,c);\r
            const priceLabel = \`$\${price}\`;\r
            html += \`<div class="cell"><div class="\${cls}" data-hour="\${h}" data-court="\${encodeURIComponent(c)}" title="Available \${priceLabel}"><div class="slot-label">Available</div><div class="slot-price">\${priceLabel}</div></div></div>\`;\r
          });\r
        }\r
        html += '</div>';\r
        scheduleEl.innerHTML = html;\r
\r
        scheduleEl.querySelectorAll('.slot-ok,.slot-full').forEach(cell=>{\r
          cell.addEventListener('click',()=>{\r
            const h = parseInt(cell.getAttribute('data-hour'),10);\r
            const c = decodeURIComponent(cell.getAttribute('data-court'));\r
            ensureHour(h);\r
            scheduleData[h][c] = scheduleData[h][c] === 'ok' ? 'full' : 'ok';\r
            renderSchedule(start,end);\r
          });\r
        });\r
      }\r
\r
      // 6. 初始化与事件绑定\r
    // 初始化下拉菜单选项，设置默认的营业时间（08:00 - 24:00）。\r
    buildHourOptions(openHour,8,23); // 开�?0..23\r
    buildHourOptions(closeHour,24,24); // 结束 0..24（�\r
    // 监听时间选择器的变化，实时更新排程表。\r
    openHour?.addEventListener('change',()=>renderSchedule(+openHour.value,+closeHour.value));\r
    closeHour?.addEventListener('change',()=>renderSchedule(+openHour.value,+closeHour.value));\r
\r
    // 绑定添加场地按钮事件\r
    // 验证输入是否为空或重复\r
    addCourtBtn?.addEventListener('click',()=>{\r
      const name = (courtNameEl.value||'').trim();\r
      if(!name) return;\r
      if(courts.includes(name)) return;\r
      courts.push(name);\r
      courtNameEl.value='';\r
      renderCourts();\r
      renderSchedule(+openHour.value,+closeHour.value);\r
    });\r
\r
    // 初始渲染\r
    renderCourts();\r
    renderSchedule(8,24);\r
\r
    // 7. 信息弹窗逻辑 (Privacy, Terms, Help)\r
    // 处理页脚链接点击事件，显示对应的信息弹窗\r
    const infoOverlay = document.getElementById('infoOverlay');\r
    const infoTitle = document.getElementById('infoTitle');\r
    const infoBody = document.getElementById('infoBody');\r
    const infoMap = {\r
      privacy: {\r
        title: 'Privacy',\r
        body: \`\r
          <p>We only store the information needed to manage bookings and memberships.</p>\r
          <p>Your account data is kept locally in this demo and is not shared with third parties.</p>\r
          <p>You can request deletion at any time by contacting the admin.</p>\r
        \`\r
      },\r
      terms: {\r
        title: 'Terms',\r
        body: \`\r
          <p>Bookings are first-come, first-served and subject to club capacity.</p>\r
          <p>Members must follow club rules and respect facility policies.</p>\r
          <p>Repeated no-shows may result in booking restrictions.</p>\r
        \`\r
      },\r
      help: {\r
        title: 'Help',\r
        body: \`\r
          <p>Need assistance? Start by searching for a club and selecting a time slot.</p>\r
          <p>If you cannot log in, double-check your email and password.</p>\r
          <p>Contact support at support@example.com for further help.</p>\r
        \`\r
      }\r
    };\r
\r
    const openInfo = (key) => {\r
      const data = infoMap[key];\r
      if (!data || !infoOverlay) return;\r
      if (infoTitle) infoTitle.textContent = data.title;\r
      if (infoBody) infoBody.innerHTML = data.body;\r
      infoOverlay.classList.add('open');\r
      document.body.classList.add('no-scroll');\r
    };\r
\r
    const closeInfo = () => {\r
      if (!infoOverlay) return;\r
      infoOverlay.classList.remove('open');\r
      document.body.classList.remove('no-scroll');\r
    };\r
\r
    document.querySelectorAll('.info-trigger').forEach((btn) => {\r
      btn.addEventListener('click', () => openInfo(btn.dataset.info));\r
    });\r
    infoOverlay?.addEventListener('click', (evt) => { if (evt.target === infoOverlay) closeInfo(); });\r
    infoOverlay?.querySelector('.info-close')?.addEventListener('click', closeInfo);\r
\r
    document.getElementById('y').textContent=new Date().getFullYear();\r
  <\/script>\r
\r
  <script src="auth-modal.js" defer><\/script>\r
  \r
`,Ac=`<!DOCTYPE html>\r
<html>\r
<head>\r
  <title>Debug Console</title>\r
  <style>\r
    body { font-family: monospace; padding: 20px; background: #000; color: #0f0; }\r
    #log { white-space: pre-wrap; word-wrap: break-word; }\r
  </style>\r
</head>\r
<body>\r
  <h2>Debug Console</h2>\r
  <div id="log"></div>\r
  <iframe id="app-frame" src="http://localhost:5173/" style="width: 100%; height: 600px; border: 1px solid #0f0; margin-top: 20px;"></iframe>\r
  \r
  <script>\r
    const log = document.getElementById('log');\r
    const logs = [];\r
    \r
    function addLog(msg) {\r
      logs.push(\`[\${new Date().toLocaleTimeString()}] \${msg}\`);\r
      log.textContent = logs.join('\\n');\r
      console.log(msg);\r
    }\r
    \r
    addLog('页面已加载');\r
    \r
    window.addEventListener('error', (e) => {\r
      addLog(\`❌ 错误: \${e.message}\\n文件: \${e.filename}:\${e.lineno}\`);\r
    });\r
    \r
    // 检查 iframe 内容\r
    const iframe = document.getElementById('app-frame');\r
    iframe.onload = () => {\r
      addLog('✅ iframe 已加载');\r
      try {\r
        const iframeDoc = iframe.contentDocument || iframe.contentWindow.document;\r
        const appDiv = iframeDoc?.getElementById('app');\r
        if (appDiv) {\r
          addLog(\`✅ 找到 #app div，内容: \${appDiv.innerHTML.substring(0, 100)}\`);\r
        } else {\r
          addLog('❌ 找不到 #app div');\r
        }\r
      } catch(e) {\r
        addLog(\`❌ 无法访问 iframe 内容: \${e.message}\`);\r
      }\r
    };\r
    \r
    iframe.onerror = () => {\r
      addLog('❌ iframe 加载失败');\r
    };\r
  <\/script>\r
</body>\r
</html>\r
`,Oc=`\uFEFF<!doctype html>\r
<html lang="en">\r
<head>\r
<meta charset="utf-8" />\r
<meta name="viewport" content="width=device-width,initial-scale=1" />\r
<title>Club Booking Portal</title>\r
<style>\r
  @import url('https://fonts.googleapis.com/css2?family=Manrope:wght@600;700&display=swap');\r
\r
  :root {\r
    --card: #fff;\r
    --ink: #222;\r
    --muted: #6B7280;\r
    --line: #E5E7EB;\r
    --chip: #F3F4F6;\r
    --header-height: 72px;\r
    --anim-base: cubic-bezier(.4, 0, .2, 1);\r
    --anim-pop: cubic-bezier(.2, .8, .4, 1);\r
  }\r
\r
  * {\r
    box-sizing: border-box;\r
  }\r
\r
  body {\r
    margin: 0;\r
    background: linear-gradient(#fff, #fafafa);\r
    color: var(--ink);\r
    font: 14px/1.5 system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial;\r
    animation: fadeBody .6s var(--anim-base) both;\r
  }\r
\r
  header {\r
    background: #fff;\r
    position: sticky;\r
    top: 0;\r
    z-index: 10;\r
    border: none;\r
    box-shadow: none;\r
  }\r
\r
  footer {\r
    border-top: 1px solid var(--line);\r
    background: #fff;\r
  }\r
\r
  header .wrap,\r
  footer .wrap,\r
  .wrap {\r
    max-width: 1100px;\r
    margin: 0 auto;\r
    padding: 16px;\r
  }\r
\r
  h1 {\r
    font-size: 22px;\r
    margin: 0;\r
  }\r
\r
  h3 {\r
    font-size: 15px;\r
    margin: 8px 0;\r
  }\r
\r
  .card h3 {\r
    font-family: 'Manrope', 'Segoe UI', Arial, sans-serif;\r
    font-weight: 700;\r
  }\r
\r
  .btn {\r
    border: 1px solid var(--line);\r
    background: #111;\r
    color: #fff;\r
    padding: 8px 12px;\r
    border-radius: 8px;\r
    font-size: 13px;\r
    cursor: pointer;\r
    transition:\r
      transform .15s ease,\r
      filter .2s var(--anim-pop);\r
    box-shadow: none !important;\r
    outline: none;\r
  }\r
\r
  .btn:hover {\r
    transform: translateY(-1px);\r
    box-shadow: none !important;\r
  }\r
\r
  .btn:active {\r
    transform: translateY(0);\r
    box-shadow: none !important;\r
  }\r
\r
  .btn:focus {\r
    outline: none;\r
    box-shadow: none !important;\r
  }\r
\r
  .btn:focus-visible {\r
    outline: none;\r
    box-shadow: none !important;\r
  }\r
\r
  .btn.ghost {\r
    background: #fff;\r
    color: #111;\r
  }\r
\r
  .btn.blue {\r
    background: #2563eb;\r
    border-color: #2563eb;\r
  }\r
\r
  .user-menu {\r
    position: relative;\r
    display: inline-block;\r
  }\r
  .avatar-btn {\r
    width: 38px;\r
    height: 38px;\r
    padding: 0;\r
    border-radius: 999px;\r
    border: 1px solid var(--line);\r
    background: #fff;\r
    color: var(--ink);\r
    display: inline-flex;\r
    align-items: center;\r
    justify-content: center;\r
    font-weight: 700;\r
    cursor: pointer;\r
    overflow: hidden;\r
  }\r
  .avatar-btn img {\r
    width: 100%;\r
    height: 100%;\r
    object-fit: cover;\r
    display: block;\r
  }\r
  .avatar-btn span {\r
    font-size: 13px;\r
  }\r
\r
  .user-menu .menu {\r
    position: absolute;\r
    top: calc(100% + 4px);\r
    right: 0;\r
    background: #fff;\r
    border: 1px solid var(--line);\r
    border-radius: 10px;\r
    box-shadow: 0 12px 28px rgba(15, 23, 42, .12);\r
    padding: 6px 0;\r
    min-width: 140px;\r
    z-index: 20;\r
    opacity: 0;\r
    transform: translateY(6px) scale(.98);\r
    visibility: hidden;\r
    pointer-events: none;\r
    transition:\r
      opacity .2s var(--anim-base),\r
      transform .25s var(--anim-pop),\r
      visibility 0s linear .25s;\r
  }\r
\r
  .user-menu .menu.open {\r
    opacity: 1;\r
    transform: translateY(0) scale(1);\r
    visibility: visible;\r
    pointer-events: auto;\r
    transition:\r
      opacity .2s var(--anim-base),\r
      transform .25s var(--anim-pop),\r
      visibility 0s;\r
  }\r
\r
  .user-menu .menu button {\r
    width: 100%;\r
    background: none;\r
    border: none;\r
    padding: 8px 14px;\r
    text-align: left;\r
    font-size: 13px;\r
    color: var(--ink);\r
    cursor: pointer;\r
  }\r
\r
  .user-menu .menu button:hover {\r
    background: #f3f4f6;\r
  }\r
\r
  .user-menu .menu.open button {\r
    animation: menuPop .28s var(--anim-pop) both;\r
  }\r
\r
  .user-menu .menu.open button:nth-child(2) {\r
    animation-delay: .08s;\r
  }\r
\r
  @keyframes menuPop {\r
    from {\r
      opacity: 0;\r
      transform: translateY(6px) scale(.96);\r
    }\r
    to {\r
      opacity: 1;\r
      transform: translateY(0) scale(1);\r
    }\r
  }\r
\r
  .card {\r
    background: var(--card);\r
    border: 1px solid var(--line);\r
    border-radius: 16px;\r
    box-shadow: 0 2px 8px rgba(0, 0, 0, .04);\r
    transition:\r
      transform .35s var(--anim-base),\r
      box-shadow .35s var(--anim-base);\r
  }\r
\r
  .card:hover {\r
    transform: translateY(-3px);\r
    box-shadow: 0 14px 32px rgba(15, 23, 42, .14);\r
  }\r
\r
  .card .pad {\r
    padding: 12px;\r
  }\r
\r
  .site-title-link {\r
    color: inherit;\r
    text-decoration: none;\r
  }\r
\r
  .card.club {\r
    opacity: 0;\r
    transform: translateY(18px) scale(.98);\r
    animation: cardIn .65s var(--anim-base) forwards;\r
    animation-delay: calc(var(--card-index, 0) * 70ms);\r
  }\r
\r
  .search-card {\r
    position: sticky;\r
    top: calc(var(--header-height) + 12px);\r
    z-index: 9;\r
    border: none;\r
    background: rgba(255, 255, 255, .92);\r
    backdrop-filter: blur(12px);\r
    box-shadow: 0 18px 36px rgba(15, 23, 42, .12);\r
    animation: floatCard 1.2s var(--anim-base) both;\r
  }\r
\r
  .search-card .pad {\r
    padding: 18px 20px;\r
  }\r
\r
  .search-row {\r
    display: flex;\r
    align-items: center;\r
    justify-content: space-between;\r
    gap: 12px;\r
  }\r
\r
  .search-controls {\r
    display: flex;\r
    align-items: center;\r
    gap: 14px;\r
    flex: 1 1 50%;\r
    max-width: 720px;\r
    min-height: 54px;\r
    border: 1px solid rgba(148, 163, 184, .35);\r
    border-radius: 999px;\r
    padding: 14px 18px;\r
    background: linear-gradient(180deg, #fff, #f8faff);\r
    box-shadow: 0 14px 30px rgba(15, 23, 42, .10);\r
    transition:\r
      border-color .25s var(--anim-base),\r
      box-shadow .25s var(--anim-base),\r
      transform .25s var(--anim-base);\r
  }\r
\r
  .search-controls:focus-within {\r
    border-color: #2563eb;\r
    box-shadow: 0 16px 32px rgba(37, 99, 235, .18);\r
    transform: translateY(-1px);\r
  }\r
\r
  .search-input-wrap {\r
    display: flex;\r
    align-items: center;\r
    gap: 12px;\r
    flex: 1;\r
    min-width: 0;\r
  }\r
\r
  .search-icon {\r
    position: relative;\r
    width: 18px;\r
    height: 18px;\r
    border: 2px solid #9CA3AF;\r
    border-radius: 50%;\r
    flex-shrink: 0;\r
  }\r
\r
  .search-icon::after {\r
    content: '';\r
    position: absolute;\r
    width: 8px;\r
    height: 2px;\r
    background: #9CA3AF;\r
    border-radius: 999px;\r
    bottom: -3px;\r
    right: -6px;\r
    transform: rotate(45deg);\r
  }\r
\r
  .search-controls .input {\r
    flex: 1;\r
    height: auto;\r
    border: none;\r
    padding: 0;\r
    background: transparent;\r
    font-size: 15px;\r
    color: var(--ink);\r
  }\r
\r
  .search-controls .input::placeholder {\r
    color: rgba(107, 114, 128, .88);\r
  }\r
\r
  .search-controls .input:focus {\r
    outline: none;\r
  }\r
\r
  .search-controls .btn {\r
    height: auto;\r
    border: none;\r
    background: rgba(37, 99, 235, .12);\r
    color: #2563eb;\r
    padding: 8px 18px;\r
    border-radius: 999px;\r
    font-weight: 600;\r
    transition:\r
      background .2s ease,\r
      color .2s ease;\r
  }\r
\r
  .search-controls .btn:hover {\r
    background: rgba(37, 99, 235, .22);\r
    color: #1d4ed8;\r
  }\r
\r
  .search-controls .btn:active {\r
    background: rgba(37, 99, 235, .3);\r
  }\r
\r
  .filter-toggle {\r
    border: 1px solid rgba(148, 163, 184, .35);\r
    background: linear-gradient(180deg, #fff, #f5f7ff);\r
    color: var(--ink);\r
    min-width: 120px;\r
    height: 54px;\r
    padding: 0 18px;\r
    border-radius: 999px;\r
    font-weight: 600;\r
    letter-spacing: .01em;\r
    cursor: pointer;\r
    box-shadow: 0 14px 30px rgba(15, 23, 42, .10);\r
    transition:\r
      transform .2s var(--anim-base),\r
      box-shadow .2s var(--anim-base),\r
      background .2s var(--anim-base);\r
  }\r
\r
  .filter-toggle:hover {\r
    transform: translateY(-1px);\r
    box-shadow: 0 12px 30px rgba(15, 23, 42, .12);\r
  }\r
\r
  .filter-toggle.active {\r
    background: #111;\r
    color: #fff;\r
    border-color: #111;\r
  }\r
\r
  .filter-count {\r
    display: inline-flex;\r
    align-items: center;\r
    justify-content: center;\r
    min-width: 18px;\r
    height: 18px;\r
    margin-left: 6px;\r
    padding: 0 6px;\r
    border-radius: 999px;\r
    background: #111;\r
    color: #fff;\r
    font-size: 11px;\r
  }\r
\r
  .filter-surface {\r
    display: none;\r
    margin-top: 12px;\r
    border: 1px solid var(--line);\r
    background: #fff;\r
    border-radius: 18px;\r
    padding: 12px 14px;\r
    box-shadow: 0 12px 30px rgba(15, 23, 42, .08);\r
  }\r
\r
  .filter-surface.open {\r
    display: block;\r
  }\r
\r
  .filter-surface__body {\r
    display: none;\r
    gap: 10px;\r
    margin-top: 0;\r
  }\r
\r
  .filter-surface.open .filter-surface__body {\r
    display: grid;\r
  }\r
\r
  .filter-panel {\r
    display: flex;\r
    gap: 8px;\r
    flex-wrap: wrap;\r
  }\r
\r
  .filter-chip {\r
    border: 1px solid var(--line);\r
    background: var(--chip);\r
    border-radius: 999px;\r
    padding: 6px 12px;\r
    font-size: 12px;\r
    cursor: pointer;\r
    transition: transform .15s var(--anim-base), background .15s var(--anim-base);\r
  }\r
\r
  .filter-chip:hover {\r
    transform: translateY(-1px);\r
    background: #fff;\r
  }\r
\r
  .filter-chip.active {\r
    background: #111;\r
    color: #fff;\r
    border-color: #111;\r
  }\r
\r
  input.input {\r
    width: 100%;\r
    height: 36px;\r
    border-radius: 8px;\r
    border: 1px solid var(--line);\r
    background: #fff;\r
    padding: 0 10px;\r
  }\r
\r
  .cards {\r
    display: grid;\r
    grid-template-columns: 1fr;\r
    gap: 8px;\r
    margin-top: 16px;\r
  }\r
\r
  @media (min-width: 640px) {\r
    .cards {\r
      grid-template-columns: 1fr 1fr;\r
    }\r
  }\r
\r
  .club {\r
    padding: 8px;\r
  }\r
\r
  .thumb {\r
    height: 64px;\r
    background: #f3f4f6;\r
    border: 1px dashed var(--line);\r
    border-radius: 10px;\r
    color: var(--muted);\r
    display: flex;\r
    align-items: center;\r
    justify-content: center;\r
  }\r
\r
  .page {\r
    padding: 20px 0;\r
  }\r
\r
  .muted {\r
    color: var(--muted);\r
    font-size: 12px;\r
  }\r
\r
  .toolbar {\r
    display: flex;\r
    gap: 8px;\r
    margin-top: 6px;\r
  }\r
\r
  .chips {\r
    display: flex;\r
    gap: 8px;\r
    flex-wrap: wrap;\r
  }\r
\r
  .chip {\r
    background: var(--chip);\r
    border: 1px solid var(--line);\r
    border-radius: 999px;\r
    padding: 3px 8px;\r
    font-size: 12px;\r
    cursor: pointer;\r
    font: inherit;\r
    appearance: none;\r
  }\r
\r
  .chip-link:hover {\r
    background: #fff;\r
  }\r
\r
  .info-overlay {\r
    position: fixed;\r
    inset: 0;\r
    display: none;\r
    align-items: center;\r
    justify-content: center;\r
    padding: 24px;\r
    background: rgba(10, 12, 16, 0.45);\r
    z-index: 3000;\r
  }\r
\r
  .info-overlay.open {\r
    display: flex;\r
  }\r
\r
  .info-card {\r
    position: relative;\r
    width: min(560px, 92vw);\r
    background: #fff;\r
    border-radius: 16px;\r
    box-shadow: 0 24px 60px rgba(10, 12, 16, 0.25);\r
    padding: 20px 20px 18px;\r
  }\r
\r
  .info-card h3 {\r
    margin: 0 0 8px;\r
    font-size: 18px;\r
  }\r
\r
  .info-body p {\r
    margin: 0 0 10px;\r
    color: var(--muted);\r
    font-size: 13px;\r
  }\r
\r
  .info-close {\r
    position: absolute;\r
    top: 10px;\r
    right: 10px;\r
    width: 30px;\r
    height: 30px;\r
    border-radius: 999px;\r
    border: 1px solid var(--line);\r
    background: #fff;\r
    cursor: pointer;\r
    color: var(--muted);\r
  }\r
\r
  @keyframes fadeBody {\r
    from { opacity: 0; transform: translateY(12px); }\r
    to { opacity: 1; transform: none; }\r
  }\r
\r
  @keyframes cardIn {\r
    0% { opacity: 0; transform: translateY(20px) scale(.97); }\r
    60% { opacity: 1; transform: translateY(-4px) scale(1); }\r
    100% { opacity: 1; transform: none; }\r
  }\r
\r
  @keyframes floatCard {\r
    from { opacity: 0; transform: translateY(30px); }\r
    to { opacity: 1; transform: none; }\r
  }\r
\r
  .empty {\r
    padding: 16px;\r
    text-align: center;\r
    color: var(--muted);\r
  }\r
\r
  @media (max-width: 480px) {\r
    .search-card .pad {\r
      padding: 14px;\r
    }\r
\r
    .search-row {\r
      flex-direction: column;\r
      align-items: stretch;\r
    }\r
\r
    .search-controls {\r
      flex-direction: column;\r
      align-items: stretch;\r
      padding: 14px;\r
      max-width: none;\r
    }\r
\r
    .search-input-wrap {\r
      width: 100%;\r
    }\r
\r
    .search-icon {\r
      display: none;\r
    }\r
\r
    .search-controls .btn {\r
      width: 100%;\r
    }\r
\r
    .filter-toggle {\r
      width: 100%;\r
    }\r
\r
  }\r
\r
  @media (prefers-reduced-motion: reduce) {\r
    *,\r
    *::before,\r
    *::after {\r
      animation-duration: 0.01ms !important;\r
      animation-iteration-count: 1 !important;\r
      transition-duration: 0.01ms !important;\r
      scroll-behavior: auto !important;\r
    }\r
  }\r
</style>\r
\r
\r
  <link rel="stylesheet" href="theme.css">\r
</head>\r
<body>\r
  <!-- 顶部导航栏：包含网站标题和用户登录/注册入口 -->\r
  <header id="top">\r
    <div class="wrap" style="display:flex;align-items:center;justify-content:space-between;gap:12px;">\r
      <h1 class="site-title" style="display:flex;align-items:center;">\r
        <a class="site-title-link" href="#top">Club Booking Portal</a>\r
      </h1>\r
      <div id="userArea" style="display:flex;align-items:center;gap:8px;margin-top:0">\r
        <button id="btnLogin" class="btn" type="button" style="min-width:80px">Login</button>\r
        <button id="btnRegister" class="btn blue" type="button" style="min-width:80px">Register</button>\r
      </div>\r
    </div>\r
  </header>\r
\r
  <div class="wrap">\r
    <!-- 搜索与筛选区域：包含搜索框、清除按钮和筛选折叠面板 -->\r
    <section id="p1" class="page">\r
      <div class="card search-card"><div class="pad">\r
        <div class="search-row">\r
          <div class="search-controls" role="search">\r
            <div class="search-input-wrap">\r
              <span class="search-icon" aria-hidden="true"></span>\r
              <input id="kw" class="input" type="text" placeholder="Try: badminton, yoga, football..." />\r
            </div>\r
            <button class="btn ghost" type="button" id="btnClear">Clear</button>\r
          </div>\r
          <button class="filter-toggle" type="button" id="filterToggle" aria-expanded="false" aria-controls="filterSurface">\r
            Filter <span class="filter-count" id="filterCount" style="display:none">0</span>\r
          </button>\r
        </div>\r
        <div class="filter-surface" id="filterSurface">\r
          <div class="filter-surface__body">\r
            <div class="filter-panel" id="filterPanel" aria-label="Filter clubs by type"></div>\r
          </div>\r
        </div>\r
      </div></div>\r
\r
      <!-- 俱乐部卡片列表容器：动态渲染搜索结果 -->\r
      <div id="cards" class="cards"></div>\r
      <div id="empty" class="empty" style="display:none">No clubs match your keyword.</div>\r
    </section>\r
  </div>\r
  <!-- 页脚：包含版权信息和辅助链接（隐私、条款、帮助） -->\r
  <footer class="site-footer">\r
    <div class="site-footer__wrap">\r
      <span class="site-footer__text">&copy; 2026 Club Booking Portal</span>\r
      <div class="site-footer__actions">\r
        <button class="site-footer__btn info-trigger" type="button" data-info="privacy">Privacy</button>\r
        <button class="site-footer__btn info-trigger" type="button" data-info="terms">Terms</button>\r
        <button class="site-footer__btn info-trigger" type="button" data-info="help">Help</button>\r
      </div>\r
    </div>\r
  </footer>\r
\r
  <!-- 信息弹窗：用于显示隐私政策、服务条款等静态内容 -->\r
  <div id="infoOverlay" class="info-overlay" aria-hidden="true">\r
    <div class="info-card" role="dialog" aria-modal="true" aria-labelledby="infoTitle">\r
      <button class="info-close" type="button" aria-label="Close">×</button>\r
      <h3 id="infoTitle">Privacy</h3>\r
      <div id="infoBody" class="info-body"></div>\r
    </div>\r
  </div>\r
\r
  <script>\r
document.addEventListener('DOMContentLoaded', () => {\r
  // 1. 布局计算：头部高度\r
  // 计算 header 的实际高度，并设置 CSS 变量 --header-height\r
  // 这确保了 sticky 定位的搜索栏能够正确地吸附在 header 下方，不会被遮挡\r
  const rootEl = document.documentElement;\r
  const headerEl = document.querySelector('header');\r
  const topLink = document.querySelector('.site-title-link');\r
  const setHeaderH = () => {\r
    if (headerEl) rootEl.style.setProperty('--header-height', \`\${headerEl.offsetHeight}px\`);\r
  };\r
  setHeaderH();\r
  window.addEventListener('resize', setHeaderH);\r
  topLink?.addEventListener('click', (event) => {\r
    event.preventDefault();\r
    window.scrollTo({ top: 0, behavior: 'smooth' });\r
  });\r
\r
  // 2. 用户会话管理\r
  // 从 localStorage 读取用户信息，判断当前是未登录、普通用户还是俱乐部管理员\r
  const userArea = document.getElementById('userArea');\r
  let savedUser = null;\r
  try {\r
    savedUser = JSON.parse(localStorage.getItem('loggedUser') || 'null');\r
  } catch {\r
    localStorage.removeItem('loggedUser');\r
  }\r
\r
  // TODO: Replace with real backend endpoint.\r
  const fetchProfile = async () => {\r
    try {\r
      const res = await fetch('/api/profile', { credentials: 'include' });\r
      if (res.ok) return await res.json();\r
    } catch (err) {\r
      console.error(err);\r
    }\r
    return null;\r
  };\r
\r
  // 如果用户已登录，渲染包含“我的/俱乐部”按钮和下拉菜单的用户区域\r
  if (savedUser && userArea) {\r
    const isClub = savedUser.type === 'club';\r
    const targetHref = isClub ? 'club.html' : 'user.html';\r
    const avatarData = (() => {\r
      try { return JSON.parse(localStorage.getItem('profileAvatar') || 'null'); } catch { return null; }\r
    })();\r
    const fallback = (savedUser.name || savedUser.email || (isClub ? 'C' : 'M')).trim().charAt(0).toUpperCase();\r
    userArea.innerHTML = \`\r
      <div class="user-menu">\r
        <button id="btnMy" class="avatar-btn" type="button" aria-haspopup="true" aria-expanded="false">\r
          \${avatarData ? \`<img src="\${avatarData}" alt="My profile">\` : \`<span>\${fallback}</span>\`}\r
        </button>\r
        <div class="menu" id="myMenu" role="menu">\r
          <button type="button" id="btnDetails" role="menuitem">Details</button>\r
          <button type="button" id="btnLogout" role="menuitem">Logout</button>\r
        </div>\r
      </div>\`;\r
\r
    const myBtn = document.getElementById('btnMy');\r
    const menuEl = document.getElementById('myMenu');\r
\r
    fetchProfile().then((profile) => {\r
      if (!profile || !myBtn) return;\r
      const avatarUrl = profile.avatarUrl || profile.avatar;\r
      const displayName = profile.displayName || profile.name || '';\r
      const email = profile.email || '';\r
      const letter = (displayName || email || fallback).trim().charAt(0).toUpperCase();\r
      if (avatarUrl) {\r
        myBtn.innerHTML = \`<img src="\${avatarUrl}" alt="My profile">\`;\r
      } else if (letter) {\r
        myBtn.innerHTML = \`<span>\${letter}</span>\`;\r
      }\r
    });\r
\r
    // 绑定下拉菜单中的按钮事件\r
    document.getElementById('btnDetails')?.addEventListener('click', () => { window.location.href = targetHref; });\r
    document.getElementById('btnLogout')?.addEventListener('click', () => { \r
      localStorage.removeItem('loggedUser'); \r
      localStorage.removeItem('user'); \r
      localStorage.removeItem('selectedClub'); \r
      location.reload(); \r
    });\r
\r
    // 控制下拉菜单的显示与隐藏（支持点击和鼠标悬停）\r
    const toggleMenu = (open) => {\r
      const wantOpen = open ?? !menuEl.classList.contains('open');\r
      menuEl.classList.toggle('open', wantOpen);\r
      myBtn.setAttribute('aria-expanded', wantOpen ? 'true' : 'false');\r
    };\r
\r
    myBtn.addEventListener('click', () => toggleMenu());\r
    const menuWrap = userArea.querySelector('.user-menu');\r
    let closeTimer = null;\r
    const cancelClose = () => {\r
      if (closeTimer) {\r
        clearTimeout(closeTimer);\r
        closeTimer = null;\r
      }\r
    };\r
    const scheduleClose = () => {\r
      cancelClose();\r
      closeTimer = setTimeout(() => toggleMenu(false), 600);\r
    };\r
    if (menuWrap) {\r
      menuWrap.addEventListener('mouseenter', () => {\r
        cancelClose();\r
        toggleMenu(true);\r
      });\r
      menuWrap.addEventListener('mouseleave', scheduleClose);\r
    }\r
    document.addEventListener('click', (e) => { if (!userArea.contains(e.target)) toggleMenu(false); });\r
    [myBtn, menuEl].forEach(el => el.addEventListener('keydown', (e) => {\r
      if (e.key === 'Escape') { toggleMenu(false); myBtn.focus(); }\r
    }));\r
  } else {\r
    const openAuth = (mode) => {\r
      if (window.openAuthModal) return window.openAuthModal(mode);\r
      window.location.href = mode === 'register' ? 'login.html#register' : 'login.html#login';\r
    };\r
    document.getElementById('btnLogin')?.addEventListener('click', () => openAuth('login'));\r
    document.getElementById('btnRegister')?.addEventListener('click', () => openAuth('register'));\r
  }\r
\r
  // 3. 数据源定义\r
  // 本地模拟的俱乐部数据列表。在实际生产环境中，这些数据通常通过 API 从后端获取。\r
  const LOCAL_CLUBS = [\r
    { id:'badminton', name:'Club 1', tags:['badminton'], desc:'Suitable for all levels, regular evening training and friendlies.' },\r
    { id:'basketball', name:'Club 2', tags:['basketball'], desc:'Varsity training + amateur matches, new members welcome.' },\r
    { id:'football', name:'Club 3', tags:['football'], desc:'Weekly matches and local tournaments, join our squad!' },\r
    { id:'yoga', name:'Club 4', tags:['yoga'], desc:'Relax and strengthen your body and mind, small group sessions.' },\r
    { id:'tennis', name:'Club 5', tags:['tennis'], desc:'Book your court and join tournaments or casual games.' },\r
    { id:'volleyball', name:'Club 6', tags:['volleyball'], desc:'Indoor and beach volleyball activities for all skill levels.' },\r
    { id:'cycling', name:'Club 7', tags:['cycling'], desc:'Weekend city rides and countryside cycling challenges.' },\r
    { id:'swimming', name:'Club 8', tags:['swimming'], desc:'Morning and evening sessions available, professional coach guidance.' },\r
    { id:'running', name:'Club 9', tags:['running'], desc:'Morning jogs, half marathons, and social running events.' },\r
    { id:'fitness', name:'Club 10', tags:['fitness'], desc:'State-of-the-art gym equipment and personalized workout plans.' },\r
    { id:'dance', name:'Club 11', tags:['dance'], desc:'Learn hip-hop, jazz, and modern dance from experienced instructors.' },\r
    { id:'badminton-east', name:'Club 12', tags:['badminton'], desc:'East campus sessions, beginner friendly with spare rackets.' },\r
    { id:'badminton-social', name:'Club 13', tags:['badminton'], desc:'Casual games, social mix-ins on Fridays.' },\r
    { id:'yoga-ii', name:'Club 14', tags:['yoga'], desc:'Intermediate vinyasa and power yoga classes.' },\r
    { id:'yoga-sunrise', name:'Club 15', tags:['yoga'], desc:'Sunrise outdoor yoga, mats provided.' },\r
    { id:'football-evening', name:'Club 16', tags:['football'], desc:'Evening league matches, 7-a-side format.' },\r
    { id:'football-academy', name:'Club 17', tags:['football'], desc:'Skill drills and coaching for developing players.' },\r
    { id:'tabletennis', name:'Club 18', tags:['table tennis'], desc:'Weekend open training, tables and equipment provided.' },\r
    { id:'tennis-advanced', name:'Club 19', tags:['tennis'], desc:'Advanced ladder matches and coaching clinics.' },\r
    { id:'tennis-social', name:'Club 20', tags:['tennis'], desc:'Casual doubles and weekend socials.' }\r
  ];\r
  let CLUBS = LOCAL_CLUBS; \r
\r
  // 4. 页面逻辑与渲染\r
  const cardsEl = document.getElementById('cards');\r
  const emptyEl = document.getElementById('empty');\r
  const kwEl = document.getElementById('kw');\r
  const filterToggle = document.getElementById('filterToggle');\r
  const filterSurface = document.getElementById('filterSurface');\r
  const filterPanel = document.getElementById('filterPanel');\r
  const filterCount = document.getElementById('filterCount');\r
  const selectedTags = [];\r
  const infoOverlay = document.getElementById('infoOverlay');\r
  const infoTitle = document.getElementById('infoTitle');\r
  const infoBody = document.getElementById('infoBody');\r
  \r
  // 静态信息内容映射（隐私、条款、帮助）\r
  const infoMap = {\r
    privacy: {\r
      title: 'Privacy',\r
      body: \`\r
        <p>We only store the information needed to manage bookings and memberships.</p>\r
        <p>Your account data is kept locally in this demo and is not shared with third parties.</p>\r
        <p>You can request deletion at any time by contacting the admin.</p>\r
      \`\r
    },\r
    terms: {\r
      title: 'Terms',\r
      body: \`\r
        <p>Bookings are first-come, first-served and subject to club capacity.</p>\r
        <p>Members must follow club rules and respect facility policies.</p>\r
        <p>Repeated no-shows may result in booking restrictions.</p>\r
      \`\r
    },\r
    help: {\r
      title: 'Help',\r
      body: \`\r
        <p>Need assistance? Start by searching for a club and selecting a time slot.</p>\r
        <p>If you cannot log in, double-check your email and password.</p>\r
        <p>Contact support at support@example.com for further help.</p>\r
      \`\r
    }\r
  };\r
\r
  // 辅助函数：获取俱乐部的主运动类型（用于显示标签）\r
  const getSportType = (club) => (club.tags && club.tags.length ? club.tags[0] : 'sport');\r
\r
  // 辅助函数：记录未登录用户的加入意图\r
  // 当用户未登录点击 Join 时，记录目标俱乐部 ID，以便登录后自动重定向到该俱乐部的加入页面\r
  const storeJoinIntent = (clubId) => {\r
    if (!clubId) return;\r
    const club = CLUBS.find(c => c.id === clubId);\r
    try {\r
      localStorage.setItem('selectedClub', JSON.stringify(club || { id: clubId }));\r
    } catch {\r
      // 忽略存储配额问题\r
    }\r
    try {\r
      localStorage.setItem('postLoginRedirect', JSON.stringify({ page: 'join', clubId }));\r
    } catch {\r
      // 忽略存储配额问题\r
    }\r
  };\r
\r
  // 全局事件处理：处理“Join Now”按钮点击\r
  // 检查登录状态，未登录则弹出登录框，已登录则跳转\r
  window.handleJoinClick = function(clubId, event) {\r
    event.preventDefault();\r
    event.stopPropagation();\r
    \r
    // 仅检查登录状态缓存，避免使用用户自动恢复数据\r
    let logged = null;\r
    try { \r
      logged = JSON.parse(localStorage.getItem('loggedUser') || 'null'); \r
    } catch(e){ \r
      logged = null; \r
    }\r
    \r
    if (!logged) {\r
      storeJoinIntent(clubId);\r
      if (window.openAuthModal) {\r
        window.openAuthModal('login');\r
      } else {\r
        window.location.href = 'login.html#login';\r
      }\r
      return false;\r
    }\r
    \r
    const club = CLUBS.find(c => c.id === clubId);\r
    if (club) {\r
      goToJoinPage(club);\r
    }\r
    return false;\r
  };\r
\r
  // 模板函数：生成单个俱乐部卡片的 HTML 结构\r
  const clubCard = (club, idx = 0) => {\r
    const sport = getSportType(club);\r
    return \`\r
    <div class="card club" data-id="\${club.id}" style="--card-index:\${idx};">\r
      <div class="thumb">Cover</div>\r
      <h3>\${club.name}</h3>\r
      <p class="muted">\${club.desc}</p>\r
      <div class="chips">\r
        <button class="chip chip-link" type="button" data-tag="\${sport}"># \${sport}</button>\r
      </div>\r
      <div class="toolbar">\r
        <button class="btn btn-join" data-action="join" data-id="\${club.id}" onclick="return window.handleJoinClick('\${club.id}', event);">Join Now</button>\r
      </div>\r
    </div>\`;\r
  };\r
\r
  // 导航函数：跳转到加入页面 (join.html)\r
  const goToJoinPage = (clubIdOrClub) => {\r
    // 兼容传入对象或俱乐部编号字符串\r
    const club = (typeof clubIdOrClub === 'string') \r
      ? CLUBS.find(c => c.id === clubIdOrClub) \r
      : clubIdOrClub;\r
    \r
    if (!club) return;\r
    try {\r
      localStorage.setItem('selectedClub', JSON.stringify(club));\r
    } catch {\r
      // 忽略存储配额问题\r
    }\r
    const query = club.id ? \`?club=\${encodeURIComponent(club.id)}\` : '';\r
    window.location.href = \`join.html\${query}\`;\r
  };\r
\r
  // 渲染函数：将俱乐部列表渲染到页面上，处理空状态\r
  const render = (list) => {\r
    if (!list.length) {\r
      cardsEl.innerHTML = '';\r
      emptyEl.style.display = 'block';\r
    } else {\r
      emptyEl.style.display = 'none';\r
      cardsEl.innerHTML = list.map((club, idx) => clubCard(club, idx)).join('');\r
    }\r
  };\r
\r
  // 弹窗控制：打开信息弹窗\r
  const openInfo = (key) => {\r
    if (!infoOverlay) return;\r
    const data = infoMap[key];\r
    if (!data) return;\r
    if (infoTitle) infoTitle.textContent = data.title;\r
    if (infoBody) infoBody.innerHTML = data.body;\r
    infoOverlay.classList.add('open');\r
    document.body.classList.add('no-scroll');\r
  };\r
\r
  // 弹窗控制：关闭信息弹窗\r
  const closeInfo = () => {\r
    if (!infoOverlay) return;\r
    infoOverlay.classList.remove('open');\r
    document.body.classList.remove('no-scroll');\r
  };\r
\r
  document.querySelectorAll('.info-trigger').forEach((btn) => {\r
    btn.addEventListener('click', () => openInfo(btn.dataset.info));\r
  });\r
  infoOverlay?.addEventListener('click', (evt) => { if (evt.target === infoOverlay) closeInfo(); });\r
  infoOverlay?.querySelector('.info-close')?.addEventListener('click', closeInfo);\r
\r
  // 筛选面板控制：切换筛选面板的显示/隐藏\r
  filterToggle?.addEventListener('click', () => {\r
    if (!filterSurface) return;\r
    const open = filterSurface.classList.toggle('open');\r
    filterToggle.setAttribute('aria-expanded', open ? 'true' : 'false');\r
  });\r
\r
  // 5. 搜索与筛选逻辑\r
  const norm = (s) => (s || '').toLowerCase().replace(/\\s+/g,'').trim();\r
  const tagKey = (tag) => norm(tag);\r
\r
  // 更新筛选 UI 状态（高亮选中标签，更新计数器）\r
  const updateFilterUI = () => {\r
    if (filterPanel) {\r
      filterPanel.querySelectorAll('.filter-chip').forEach((btn) => {\r
        btn.classList.toggle('active', selectedTags.includes(btn.dataset.key));\r
      });\r
    }\r
    if (filterToggle) filterToggle.classList.toggle('active', selectedTags.length > 0);\r
    if (filterCount) {\r
      if (selectedTags.length) {\r
        filterCount.style.display = 'inline-flex';\r
        filterCount.textContent = String(selectedTags.length);\r
      } else {\r
        filterCount.style.display = 'none';\r
      }\r
    }\r
  };\r
\r
  // 渲染筛选标签（Chips）：从所有俱乐部中提取标签，去重并排序\r
  const renderFilterChips = () => {\r
    if (!filterPanel) return;\r
    const seen = new Set();\r
    const tags = [];\r
    CLUBS.forEach((club) => {\r
      (club.tags || []).forEach((tag) => {\r
        const key = tagKey(tag);\r
        if (!seen.has(key)) {\r
          seen.add(key);\r
          tags.push({ key, label: tag });\r
        }\r
      });\r
    });\r
    tags.sort((a, b) => a.label.localeCompare(b.label));\r
    filterPanel.innerHTML = tags.map(tag => (\r
      \`<button class="filter-chip" type="button" data-key="\${tag.key}"># \${tag.label}</button>\`\r
    )).join('');\r
    filterPanel.querySelectorAll('.filter-chip').forEach((btn) => {\r
      btn.addEventListener('click', () => {\r
        const key = btn.dataset.key;\r
        const idx = selectedTags.indexOf(key);\r
        if (idx >= 0) {\r
          selectedTags.splice(idx, 1);\r
        } else if (selectedTags.length < 2) {\r
          selectedTags.push(key);\r
        } else {\r
          return;\r
        }\r
        updateFilterUI();\r
        filter();\r
      });\r
    });\r
    updateFilterUI();\r
  };\r
\r
  // 核心过滤函数：结合关键字搜索和标签筛选\r
  const filter = () => {\r
    const q = norm(kwEl?.value);\r
    const list = CLUBS.filter(c => {\r
      const hay = norm(c.name) + ' ' + norm((c.tags || []).join(' '));\r
      const matchText = !q || hay.includes(q);\r
      const matchTag = !selectedTags.length || (c.tags || []).some(t => selectedTags.includes(tagKey(t)));\r
      return matchText && matchTag;\r
    });\r
    render(list);\r
  };\r
\r
  // TODO: Replace with backend data fetch.\r
  // Expected shape: [{ id, name, desc, tags: [] }]\r
  const loadClubs = async () => {\r
    try {\r
      const res = await fetch('/api/clubs', { credentials: 'include' });\r
      if (!res.ok) throw new Error('network');\r
      const data = await res.json();\r
      if (Array.isArray(data) && data.length) {\r
        CLUBS = data.map((c, idx) => ({\r
          id: c.id || \`club-\${idx + 1}\`,\r
          name: c.name || \`Club \${idx + 1}\`,\r
          desc: c.desc || c.description || '',\r
          tags: Array.isArray(c.tags) && c.tags.length ? c.tags : ['sport']\r
        }));\r
        return;\r
      }\r
    } catch (err) {\r
      // fallback to local\r
    }\r
    CLUBS = LOCAL_CLUBS;\r
  };\r
\r
  const initData = async () => {\r
    await loadClubs();\r
    renderFilterChips();\r
    filter();\r
  };\r
\r
  // 事件委托：处理卡片上的标签点击，快速筛选\r
  cardsEl?.addEventListener('click', (event) => {\r
    const chip = event.target.closest('.chip-link');\r
    if (!chip || !cardsEl.contains(chip)) return;\r
    const tag = chip.dataset.tag;\r
    if (!tag) return;\r
    selectedTags.length = 0;\r
    selectedTags.push(tagKey(tag));\r
    updateFilterUI();\r
    filter();\r
    if (filterSurface && filterToggle) {\r
      filterSurface.classList.add('open');\r
      filterToggle.setAttribute('aria-expanded', 'true');\r
    }\r
  });\r
\r
  // 绑定搜索框事件\r
  document.getElementById('btnClear')?.addEventListener('click', () => { if (kwEl){ kwEl.value = ''; kwEl.focus(); } filter(); });\r
  kwEl?.addEventListener('input', filter);\r
  kwEl?.addEventListener('keydown', (e) => { if (e.key === 'Enter') filter(); });\r
\r
  // 页面加载完成后执行一次初始筛选渲染\r
  initData();\r
});\r
  <\/script>\r
\r
  <script src="auth-modal.js" defer><\/script>\r
</body>\r
</html>\r
`,Nc=`<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width,initial-scale=1" />
  <title>Club Portal (MVP)</title>
  <link rel="stylesheet" href="theme.css">
  <style>
    body { font-family: Arial, sans-serif; margin: 24px; }
    .row { display: flex; gap: 24px; }
    .card { border: 1px solid #ddd; border-radius: 8px; padding: 12px; width: 360px; }
    .item { padding: 8px; border-bottom: 1px solid #eee; cursor: pointer; }
    .item:last-child { border-bottom: none; }
    .muted { color: #666; font-size: 12px; }
    .title { font-weight: 600; }
    button { padding: 6px 10px; }
    input { padding: 6px; width: 100%; box-sizing: border-box; margin: 6px 0; }
    .mono { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace; font-size: 12px; }
  </style>
</head>
<body>
  <a class="link-back" href="home.html">Back to Home</a>
  <!-- 调试版入口页面：用于后端接口联调与数据查看 -->
  <h1>Club Enrolment Portal (MVP)</h1>

  <div class="row">
    <!-- 俱乐部列表：点击条目会加载对应的可预约时段 -->
    <div class="card">
      <div class="title">Clubs</div>
      <div class="muted">Click a club to load timeslots</div>
      <div id="clubs"></div>
    </div>

    <!-- 时段列表与预约：可手动输入用户与时间范围做测试 -->
    <div class="card">
      <div class="title">Timeslots</div>
      <div class="muted">Range (ISO):</div>
      <input id="userId" placeholder="userId" value="1" />
      <input id="userMembershipId" placeholder="userMembershipId (optional)" value="2" />
      <input id="from" placeholder="from, e.g. 2026-01-16T00:00:00" value="2026-01-16T00:00:00" />
      <input id="to" placeholder="to, e.g. 2026-01-17T00:00:00" value="2026-01-17T00:00:00" />
      <button id="reload">Reload timeslots</button>
      <div class="muted" id="lastUpdated"></div>
      <div class="muted" id="selectedClub"></div>
      <div class="muted" id="bookResult"></div>
      <div id="timeslots"></div>
    </div>
  </div>

  <!-- 我的预约记录：用于验证预约/取消接口是否生效 -->
  <div class="card" style="margin-top:24px; width: 760px;">
    <div class="title">My Bookings (userId=1)</div>
    <button id="loadBookings">Reload bookings</button>
    <div id="bookings"></div>
  </div>

  <!-- 调试输出：展示接口响应或错误信息 -->
  <h3>Debug</h3>
  <pre class="mono" id="debug"></pre>

  <footer class="site-footer">
    <div class="site-footer__wrap">
      <span class="site-footer__text">&copy; 2026 Club Booking Portal</span>
      <div class="site-footer__actions">
        <button class="site-footer__btn info-trigger" type="button" data-info="privacy">Privacy</button>
        <button class="site-footer__btn info-trigger" type="button" data-info="terms">Terms</button>
        <button class="site-footer__btn info-trigger" type="button" data-info="help">Help</button>
      </div>
    </div>
  </footer>

<!-- 调试脚本：演示接口调用与渲染 -->
<script>
  // 接口基础地址（后端服务）
  const API_BASE = "http://13.40.74.21:8080";

  // 当前选中的俱乐部 ID
  let currentClubId = null;

  // 简单调试输出，统一写入页面底部区域
  function debug(msg) {
    const el = document.getElementById("debug");
    el.textContent = (typeof msg === "string") ? msg : JSON.stringify(msg, null, 2);
  }

  // GET 请求封装：返回 JSON 或文本
  async function apiGet(path) {
    const res = await fetch(\`\${API_BASE}\${path}\`, { credentials: "include" });
    const text = await res.text();
    let data;
    try { data = JSON.parse(text); } catch { data = text; }
    if (!res.ok) throw new Error(\`\${res.status} \${res.statusText}: \${typeof data === "string" ? data : (data.message || text)}\`);
    return data;
  }

  // POST 请求封装：带 JSON body
  async function apiPost(path, bodyObj) {
    const res = await fetch(\`\${API_BASE}\${path}\`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify(bodyObj)
    });
    const text = await res.text();
    let data;
    try { data = JSON.parse(text); } catch { data = text; }
    if (!res.ok) throw new Error(\`\${res.status} \${res.statusText}: \${typeof data === "string" ? data : (data.message || text)}\`);
    return data;
  }

  // 渲染俱乐部列表，并绑定点击事件
  function renderClubs(clubs) {
    const box = document.getElementById("clubs");
    box.innerHTML = "";
    if (!clubs.length) {
      box.innerHTML = '<div class="muted">No clubs found.</div>';
      return;
    }
    clubs.forEach(c => {
      const div = document.createElement("div");
      div.className = "item";
      div.innerHTML = \`<div><span class="title">\${c.clubName}</span></div><div class="muted">\${c.sportType || ""}</div>\`;
      div.onclick = () => {
        currentClubId = c.clubId;
        document.getElementById("selectedClub").textContent = \`Selected clubId = \${currentClubId}\`;
        loadTimeslots();
      };
      box.appendChild(div);
    });
  }

  // 渲染时段列表，并提供“Book”按钮
  function renderTimeslots(list) {
    const box = document.getElementById("timeslots");
    box.innerHTML = "";
    if (!list.length) {
      box.innerHTML = '<div class="muted">No timeslots in this range.</div>';
      return;
    }

    list.forEach(t => {
      const div = document.createElement("div");
      div.className = "item";

      const btn = document.createElement("button");
      btn.textContent = "Book";
      btn.onclick = async (e) => {
        e.stopPropagation();

        const userId = Number(document.getElementById("userId").value);
        const rawUm = document.getElementById("userMembershipId").value.trim();
        const userMembershipId = rawUm ? Number(rawUm) : null;

        const body = { userId, timeslotId: t.timeslotId };
        if (userMembershipId !== null) body.userMembershipId = userMembershipId;

        try {
          const result = await apiPost("/api/bookings", body);
          document.getElementById("bookResult").textContent =
            \`Booked: bookingId=\${result.bookingId}, priceToPay=\${result.priceToPay}, status=\${result.status}\`;
          debug(result);
          await loadBookings();
        } catch (err) {
          document.getElementById("bookResult").textContent = \`Book failed: \${String(err)}\`;
          debug(String(err));
        }
      };

      div.innerHTML = \`
        <div class="title">timeslotId=\${t.timeslotId} \${t.membersOnly ? "(Members only)" : ""}</div>
        <div class="muted">\${t.startTime} -> \${t.endTime}</div>
        <div class="muted">capacity=\${t.capacity}, allowBooking=\${t.allowBooking}</div>
        <div class="muted">price=\${t.price}, membersPrice=\${t.membersPrice}</div>
      \`;
      div.appendChild(btn);
      box.appendChild(div);
    });
  }

  // 加载俱乐部列表
  async function loadClubs() {
    try {
      const clubs = await apiGet("/api/clubs");
      renderClubs(clubs);
      debug({ ok: true, clubsCount: clubs.length });
    } catch (e) {
      debug(String(e));
    }
  }

  // 加载指定俱乐部在某个时间范围内的时段
  async function loadTimeslots() {
    if (!currentClubId) {
      debug("Select a club first.");
      return;
    }
    const from = encodeURIComponent(document.getElementById("from").value.trim());
    const to = encodeURIComponent(document.getElementById("to").value.trim());
    debug({ loading: true, clubId: currentClubId, from: decodeURIComponent(from), to: decodeURIComponent(to) });
    try {
      const list = await apiGet(\`/api/clubs/\${currentClubId}/timeslots?from=\${from}&to=\${to}\`);
      renderTimeslots(list);
      document.getElementById("lastUpdated").textContent = \`Last loaded: \${new Date().toISOString()}\`;
      debug({ ok: true, clubId: currentClubId, timeslotsCount: list.length });
    } catch (e) {
      debug(String(e));
    }
  }

  // 渲染我的预约记录（支持取消）
  function renderBookings(list) {
    const box = document.getElementById("bookings");
    box.innerHTML = "";
    if (!list.length) {
      box.innerHTML = '<div class="muted">No bookings.</div>';
      return;
    }

    list.forEach(b => {
      const div = document.createElement("div");
      div.className = "item";

      const isCancelled = (b.status === 2);
      const btn = document.createElement("button");
      btn.textContent = isCancelled ? "Cancelled" : "Cancel";
      btn.disabled = isCancelled;

      btn.onclick = async () => {
        try {
          const result = await apiPost(\`/api/bookings/\${b.bookingId}/cancel\`, {});
          debug(result);
          await loadBookings();
        } catch (err) {
          debug(String(err));
        }
      };

      div.innerHTML = \`
        <div class="title">bookingId=\${b.bookingId} (status=\${b.status})</div>
        <div class="muted">timeslotId=\${b.timeslotId}, priceToPay=\${b.priceToPay}, covered=\${b.isCoveredByMembership}</div>
        <div class="muted">cancelTime=\${b.cancelTime || "-"}</div>
      \`;

      div.appendChild(btn);
      box.appendChild(div);
    });
  }

  // 加载我的预约记录
  async function loadBookings() {
    try {
      const userId = Number(document.getElementById("userId").value);
      const list = await apiGet(\`/api/users/\${userId}/bookings\`);
      renderBookings(list);
      debug({ ok: true, bookingsCount: list.length });
    } catch (e) {
      debug(String(e));
    }
  }

  // 事件绑定：刷新时段与预约列表
  document.getElementById("reload").onclick = loadTimeslots;
  document.getElementById("loadBookings").onclick = loadBookings;

  // 初始加载：拉取俱乐部与预约记录
  loadClubs();
  loadBookings();
<\/script>

  <script src="auth-modal.js" defer><\/script>
</body>
</html>
`,Rc=`<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8" />
<meta name="viewport" content="width=device-width,initial-scale=1" />
<title>Club Booking Portal · Join Now</title>
  <link rel="stylesheet" href="theme.css">
<style>
  :root {
    --bg: #f7f7f8;
    --card: #fff;
    --ink: #222;
    --muted: #6b7280;
    --line: #e5e7eb;
    --chip: #f3f4f6;
    --danger: #dc2626;
  }

  * {
    box-sizing: border-box;
  }

  body {
    margin: 0;
    background: linear-gradient(#fff, #fafafa);
    color: var(--ink);
    font: 14px/1.5 system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial;
  }

  header,
  footer {
    border-bottom: 1px solid var(--line);
    background: #fff;
    position: sticky;
    top: 0;
    z-index: 10;
  }

  footer {
    border-top: 1px solid var(--line);
    border-bottom: none;
    margin-top: 32px;
  }

  header .wrap,
  footer .wrap,
  .wrap {
    max-width: 1100px;
    margin: 0 auto;
    padding: 16px;
  }

  a {
    color: inherit;
    text-decoration: none;
  }

  h1 {
    font-size: 22px;
    margin: 0;
  }

  h2 {
    font-size: 20px;
    margin: 0 0 8px;
  }

  h3 {
    font-size: 15px;
    margin: 12px 0;
  }

  .btn {
    border: 1px solid var(--line);
    background: #111;
    color: #fff;
    padding: 8px 12px;
    border-radius: 8px;
    font-size: 13px;
    cursor: pointer;
  }

  .btn.ghost {
    background: #fff;
    color: #111;
  }

  .btn.blue {
    background: #2563eb;
    border-color: #2563eb;
  }

  .btn.red {
    background: #b91c1c;
    border-color: #b91c1c;
  }

  .user-menu {
    position: relative;
    display: inline-block;
  }

  .user-menu .menu {
    position: absolute;
    top: calc(100% + 4px);
    right: 0;
    background: #fff;
    border: 1px solid var(--line);
    border-radius: 10px;
    box-shadow: 0 12px 28px rgba(15, 23, 42, .12);
    padding: 6px 0;
    display: none;
    min-width: 140px;
    z-index: 20;
  }

  .user-menu .menu.open {
    display: block;
  }

  .user-menu .menu button {
    width: 100%;
    background: none;
    border: none;
    padding: 8px 14px;
    text-align: left;
    font-size: 13px;
    color: var(--ink);
    cursor: pointer;
  }

  .user-menu .menu button:hover {
    background: #f3f4f6;
  }

  .card {
    background: var(--card);
    border: 1px solid var(--line);
    border-radius: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, .04);
  }

  .card .pad {
    padding: 16px;
  }

  .muted {
    color: var(--muted);
    font-size: 12px;
  }

  .thumb {
    height: 180px;
    background: #f3f4f6;
    border: 1px dashed var(--line);
    border-radius: 12px;
    color: var(--muted);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .chips {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    align-items: center;
  }

  .chip {
    background: var(--chip);
    border: 1px solid var(--line);
    border-radius: 999px;
    padding: 4px 10px;
    font-size: 12px;
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }

  .chip-hours {
    gap: 10px;
    padding-right: 16px;
  }

  .chip-divider {
    color: var(--line);
  }

  .crumbs {
    font-size: 12px;
    color: var(--muted);
    display: flex;
    gap: 6px;
    align-items: center;
    margin-bottom: 16px;
  }

  .crumbs a {
    color: #2563eb;
  }

  #schedule {
    overflow-x: auto;
  }

  .schedule-grid {
    display: grid;
    gap: 0;
    border: 1px solid var(--line);
    border-radius: 12px;
    overflow: hidden;
  }

  .schedule-grid .cell {
    padding: 10px 8px;
    border-top: 1px solid var(--line);
  }

  .schedule-grid .head {
    background: #f9fafb;
    font-weight: 600;
  }

  .schedule-grid .time {
    background: #f9fafb;
    border-right: 1px solid var(--line);
    color: var(--muted);
    font-size: 12px;
  }

  .slot-ok {
    padding: 8px 10px;
    margin: 6px;
    border: 1px solid #bfdbfe;
    background: #dbeafe;
    border-radius: 10px;
    text-align: center;
    cursor: pointer;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 4px;
    min-height: 44px;
  }

  .slot-full {
    padding: 8px 10px;
    margin: 6px;
    border: 1px solid #d1d5db;
    background: #e5e7eb;
    border-radius: 10px;
    text-align: center;
    cursor: pointer;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 4px;
    min-height: 44px;
  }

  .slot-label {
    font-size: 12px;
    font-weight: 600;
  }

  .slot-full .slot-label {
    color: #6b7280;
  }

  .slot-price {
    font-size: 11px;
    font-weight: 600;
    color: #1d4ed8;
    background: #eff6ff;
    border: 1px solid #bfdbfe;
    border-radius: 999px;
    padding: 2px 8px;
  }

  footer .wrap {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 8px;
    font-size: 12px;
    color: var(--muted);
  }

  @media (max-width: 720px) {
    header .wrap {
      flex-direction: column;
      align-items: flex-start;
    }

    .thumb {
      height: 140px;
    }
  }
</style>
</head>
<body>
  <!-- 顶部导航：返回首页与登录入口 -->
  <header>
    <div class="wrap" style="display:flex;align-items:center;justify-content:space-between;gap:12px">
      <h1>Club Booking Portal</h1>
      <div style="display:flex;gap:8px;align-items:center">
        <a class="link-back" href="home.html">Back to Home</a>
        <div id="userArea" style="display:flex;align-items:center;gap:8px">
          <button id="btnLogin" class="btn" type="button">Login</button>
          <button id="btnRegister" class="btn blue" type="button">Register</button>
        </div>
      </div>
    </div>
  </header>

  <div class="wrap">
    <div class="crumbs"><a href="home.html">Home</a> / <span id="crumbClub">Club</span></div>

    <!-- 俱乐部信息卡片 -->
    <div class="card" aria-live="polite"><div class="pad">
      <div class="thumb">Club cover / banner</div>
      <h2 id="clubTitle">Club</h2>
      <p class="muted" id="clubDesc">Description</p>
      <div class="chips" id="clubChips">
        <span class="chip" id="chipSport">🏸 Sport</span>
        <span class="chip" id="chipLocation">📍 Location</span>
        <span class="chip chip-hours">
          <span>🕒 Opening hours <span id="hoursLabel">--:-- - --:--</span></span>
          <span class="chip-divider">|</span>
          <span id="chipCourts">Courts: 0</span>
        </span>
      </div>
    </div></div>

    <!-- 预约时段面板 -->
    <div class="card" style="margin-top:18px"><div class="pad">
      <h3>Booking board</h3>
      <p class="muted">Tap a slot to toggle between available and full (demo only).</p>
      <div id="schedule" style="margin-top:12px"></div>
    </div></div>
  </div>

  <footer class="site-footer">
    <div class="site-footer__wrap">
      <span class="site-footer__text">&copy; 2026 Club Booking Portal</span>
      <div class="site-footer__actions">
        <button class="site-footer__btn info-trigger" type="button" data-info="privacy">Privacy</button>
        <button class="site-footer__btn info-trigger" type="button" data-info="terms">Terms</button>
        <button class="site-footer__btn info-trigger" type="button" data-info="help">Help</button>
      </div>
    </div>
  </footer>

  <script>
  document.addEventListener('DOMContentLoaded', () => {
    // ---- 需要登录才能访问 ----
    // 仅检查 loggedUser，保持与首页一致
    let currentUser = null;
    try {
      currentUser = JSON.parse(localStorage.getItem('loggedUser') || 'null');
    } catch(e) {
      currentUser = null;
    }
    if (!currentUser) {
      // 未登录，跳回首页
      window.location.href = 'home.html';
      return;
    }

    const fallbackClubs = [
      { id:'badminton', name:'Badminton Club', tags:['badminton'], desc:'Suitable for all levels, regular evening training and friendlies.' },
      { id:'basketball', name:'Basketball Club', tags:['basketball'], desc:'Varsity training + amateur matches, new members welcome.' },
      { id:'football', name:'Football Club', tags:['football'], desc:'Weekly matches and local tournaments, join our squad!' },
      { id:'yoga', name:'Yoga Club', tags:['yoga'], desc:'Relax and strengthen your body and mind, small group sessions.' },
      { id:'tabletennis', name:'Table Tennis Club', tags:['table tennis'], desc:'Weekend open training, tables and equipment provided.' },
      { id:'swimming', name:'Swimming Club', tags:['swimming'], desc:'Morning and evening sessions available, professional coach guidance.' },
      { id:'running', name:'Running Club', tags:['running'], desc:'Morning jogs, half marathons, and social running events.' },
      { id:'volleyball', name:'Volleyball Club', tags:['volleyball'], desc:'Indoor and beach volleyball activities for all skill levels.' },
      { id:'cycling', name:'Cycling Club', tags:['cycling'], desc:'Weekend city rides and countryside cycling challenges.' },
      { id:'tennis', name:'Tennis Club', tags:['tennis'], desc:'Book your court and join tournaments or casual games.' }
    ];

    const clubMeta = {
      badminton: { location:'Hall A', members:120, courts:['Court A','Court B','Court C'], hours:{ open:8, close:24 }, rating:4.7, sessions:3 },
      basketball: { location:'Arena 1', members:95, courts:['Court 1','Court 2'], hours:{ open:9, close:22 }, rating:4.4, sessions:4 },
      football: { location:'Pitch West', members:80, courts:['Pitch 1','Pitch 2'], hours:{ open:7, close:21 }, rating:4.6, sessions:5 },
      yoga: { location:'Studio B', members:60, courts:['Studio 1','Studio 2'], hours:{ open:6, close:20 }, rating:4.9, sessions:7 },
      tennis: { location:'Outdoor Courts', members:110, courts:['Court A','Court B','Court C','Court D'], hours:{ open:8, close:23 }, rating:4.5, sessions:6 },
      default: { location:'Main Hall', members:72, courts:['Court 1','Court 2'], hours:{ open:8, close:22 }, rating:4.5, sessions:4 }
    };

    // ----- 工具函数 -----
    const slug = (s = '') => s.toLowerCase().replace(/[^a-z0-9]+/g, '');
    const norm = (s = '') => s.toLowerCase().trim();

    // 根据 URL 参数或缓存选中俱乐部
    function pickClub() {
      const params = new URLSearchParams(window.location.search);
      const q = params.get('club');
      let stored = null;
      try {
        stored = JSON.parse(localStorage.getItem('selectedClub') || 'null');
      } catch {
        stored = null;
        localStorage.removeItem('selectedClub');
      }

      if (q && stored && stored.id !== q && slug(stored.id || '') !== slug(q)) {
        stored = null;
      }

      if (q && !stored) {
        stored = fallbackClubs.find(c => c.id === q || slug(c.name) === slug(q)) || null;
      }

      if (!stored) {
        stored = fallbackClubs[0];
      }
      return stored;
    }

    let activeClub = pickClub();
    let meta = clubMeta[activeClub.id] || clubMeta.default;

    // TODO: Replace with backend data fetch.
    // Expected shape:
    // GET /api/clubs/{id} -> { id, name, desc, tags, location, courts, hours, rating, sessions }
    // GET /api/clubs/{id}/schedule -> { hours:{open,close}, courts:[], schedule:{ [hour]: { [court]: 'ok'|'full' } } }
    const apiGet = async (path) => {
      try {
        const res = await fetch(path, { credentials: 'include' });
        if (res.ok) return await res.json();
      } catch (err) {
        console.error(err);
      }
      return null;
    };
    const fetchClub = (id) => apiGet(\`/api/clubs/\${encodeURIComponent(id)}\`);
    const fetchSchedule = (id) => apiGet(\`/api/clubs/\${encodeURIComponent(id)}/schedule\`);

    // ----- DOM 引用 -----
    const crumbClub = document.getElementById('crumbClub');
    const clubTitle = document.getElementById('clubTitle');
    const clubDesc = document.getElementById('clubDesc');
    const chipSport = document.getElementById('chipSport');
    const chipLocation = document.getElementById('chipLocation');
    const chipCourts = document.getElementById('chipCourts');
    const hoursLabel = document.getElementById('hoursLabel');
    const scheduleEl = document.getElementById('schedule');

    let hours = { open: meta.hours?.open ?? 8, close: meta.hours?.close ?? 22 };
    const state = {
      courts: [...new Set(meta.courts && meta.courts.length ? meta.courts : [\`\${activeClub.name} Court 1\`, \`\${activeClub.name} Court 2\`])],
      schedule: {},
      seed: activeClub.id ? activeClub.id.split('').reduce((sum, ch) => sum + ch.charCodeAt(0), 0) : 42
    };

    const applyClub = () => {
    crumbClub.textContent = activeClub.name;
    clubTitle.textContent = activeClub.name;
    document.title = \`Join \${activeClub.name} · Club Booking System\`;
    clubDesc.textContent = activeClub.desc || 'Club details unavailable.';
    chipSport.textContent = \`# \${activeClub.tags?.[0] || 'sport'}\`;
    chipLocation.textContent = \`📍 \${meta.location || 'Main hall'}\`;
    if (chipCourts) chipCourts.textContent = \`Courts: \${state.courts.length}\`;
    };

    const pad = (n) => String(n).padStart(2, '0');
    const slotPrice = (hour, court) => {
      const base = 6 + (state.seed % 5);
      const peak = hour >= 18 ? 3 : 0;
      const courtOffset = state.courts.indexOf(court) % 2;
      return base + peak + courtOffset;
    };
    const updateHoursLabel = () => {
    hoursLabel.textContent = \`\${pad(hours.open)}:00 - \${pad(hours.close)}:00\`;
    };

    function ensureHour(hour) {
      if (!state.schedule[hour]) state.schedule[hour] = {};
      state.courts.forEach((court, idx) => {
        if (!state.schedule[hour][court]) {
          const hash = (hour * 37 + idx * 19 + state.seed) % 5;
          state.schedule[hour][court] = hash === 0 ? 'full' : 'ok';
        }
      });

      Object.keys(state.schedule[hour]).forEach((court) => {
        if (!state.courts.includes(court)) delete state.schedule[hour][court];
      });
    }

    // 生成时段网格并填充内容
    function renderSchedule() {
      const start = Number(hours.open);
      const end = Number(hours.close);
      if (!Number.isFinite(start) || !Number.isFinite(end)) {
        scheduleEl.innerHTML = '<div class="muted" style="padding:12px">No schedule data.</div>';
        return;
      }
      if (end <= start) {
        scheduleEl.innerHTML = '<div class="muted" style="padding:12px">Closing hour must be later than opening hour.</div>';
        return;
      }
      if (!state.courts.length) {
        scheduleEl.innerHTML = '<div class="muted" style="padding:12px">Add at least one court or area to show the board.</div>';
        return;
      }
      for (let h = start; h < end; h++) ensureHour(h);

      const cols = state.courts.length + 1;
      const template = \`repeat(\${cols}, minmax(120px, 1fr))\`;

      let html = \`<div class="schedule-grid" style="grid-template-columns:\${template}">\`;
      html += \`<div class="cell head time">Time</div>\`;
      state.courts.forEach((court) => {
        html += \`<div class="cell head">\${court}</div>\`;
      });

      for (let hour = start; hour < end; hour++) {
        const next = hour + 1;
        html += \`<div class="cell time">\${pad(hour)}:00 - \${pad(next)}:00</div>\`;
        state.courts.forEach((court) => {
          const status = state.schedule[hour]?.[court] || 'ok';
          const cls = status === 'full' ? 'slot-full' : 'slot-ok';
          if (status === 'full') {
            html += \`<div class="cell"><div class="\${cls}" data-hour="\${hour}" data-court="\${encodeURIComponent(court)}" title="Fully booked"><div class="slot-label">Fully booked</div></div></div>\`;
            return;
          }
          const price = slotPrice(hour, court);
          const priceLabel = \`$\${price}\`;
          html += \`<div class="cell"><div class="\${cls}" data-hour="\${hour}" data-court="\${encodeURIComponent(court)}" title="Available \${priceLabel}"><div class="slot-label">Available</div><div class="slot-price">\${priceLabel}</div></div></div>\`;
        });
      }

      html += '</div>';
      scheduleEl.innerHTML = html;

      scheduleEl.querySelectorAll('.slot-ok,.slot-full').forEach((slot) => {
        slot.addEventListener('click', () => {
          const hour = Number(slot.getAttribute('data-hour'));
          const court = decodeURIComponent(slot.getAttribute('data-court'));
          ensureHour(hour);
          state.schedule[hour][court] = state.schedule[hour][court] === 'ok' ? 'full' : 'ok';
          renderSchedule();
        });
      });
    }

    const initData = async () => {
      const apiClub = await fetchClub(activeClub.id);
      if (apiClub) {
        activeClub = {
          id: apiClub.id || activeClub.id,
          name: apiClub.name || activeClub.name,
          desc: apiClub.desc || apiClub.description || activeClub.desc,
          tags: Array.isArray(apiClub.tags) && apiClub.tags.length ? apiClub.tags : activeClub.tags
        };
        meta = {
          location: apiClub.location || meta.location,
          members: apiClub.members || meta.members,
          courts: Array.isArray(apiClub.courts) && apiClub.courts.length ? apiClub.courts : meta.courts,
          hours: apiClub.hours || meta.hours,
          rating: apiClub.rating || meta.rating,
          sessions: apiClub.sessions || meta.sessions
        };
        hours = { open: meta.hours?.open ?? 8, close: meta.hours?.close ?? 22 };
        state.courts = [...new Set(meta.courts && meta.courts.length ? meta.courts : state.courts)];
      }
      const apiSchedule = await fetchSchedule(activeClub.id);
      if (apiSchedule && apiSchedule.courts && apiSchedule.hours) {
        hours = apiSchedule.hours;
        state.courts = apiSchedule.courts;
        state.schedule = apiSchedule.schedule || {};
      }
      applyClub();
      updateHoursLabel();
      renderSchedule();
    };

    applyClub();
    updateHoursLabel();
    initData();

    // ---- 复用的用户菜单逻辑 ----
    const userArea = document.getElementById('userArea');
    let savedUser = null;
    try {
      savedUser = JSON.parse(localStorage.getItem('loggedUser') || 'null');
    } catch {
      savedUser = null;
      localStorage.removeItem('loggedUser');
    }

    if (savedUser && userArea) {
      const isClub = savedUser.type === 'club';
      const targetHref = isClub ? 'club.html' : 'user.html';
      const myLabel = isClub ? 'Club' : 'My';
      userArea.innerHTML = \`
        <div class="user-menu">
          <button id="btnMy" class="btn blue" type="button" aria-haspopup="true" aria-expanded="false">\${myLabel}</button>
          <div class="menu" id="myMenu" role="menu">
            <button type="button" id="btnDetails" role="menuitem">Details</button>
            <button type="button" id="btnLogout" role="menuitem">Logout</button>
          </div>
        </div>\`;

      const myBtn = document.getElementById('btnMy');
      const menuEl = document.getElementById('myMenu');

      document.getElementById('btnDetails')?.addEventListener('click', () => { window.location.href = targetHref; });
      document.getElementById('btnLogout')?.addEventListener('click', () => { localStorage.removeItem('loggedUser'); location.reload(); });

      const toggleMenu = (open) => {
        const wantOpen = open ?? !menuEl.classList.contains('open');
        menuEl.classList.toggle('open', wantOpen);
        myBtn.setAttribute('aria-expanded', wantOpen ? 'true' : 'false');
      };

      myBtn.addEventListener('click', () => toggleMenu());
      document.addEventListener('click', (e) => { if (!userArea.contains(e.target)) toggleMenu(false); });
      [myBtn, menuEl].forEach(el => el.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') { toggleMenu(false); myBtn.focus(); }
      }));
    } else {
      const openAuth = (mode) => {
        if (window.openAuthModal) return window.openAuthModal(mode);
        window.location.href = mode === 'register' ? 'login.html#register' : 'login.html#login';
      };
      document.getElementById('btnLogin')?.addEventListener('click', () => openAuth('login'));
      document.getElementById('btnRegister')?.addEventListener('click', () => openAuth('register'));
    }
  });
  <\/script>

  <script src="auth-modal.js" defer><\/script>
</body>
</html>
`,Mc=`\uFEFF\uFEFF<!doctype html>\r
<html lang="en">\r
<head>\r
<meta charset="utf-8" />\r
<meta name="viewport" content="width=device-width,initial-scale=1" />\r
<title>Club Booking Portal</title>\r
  <link rel="stylesheet" href="theme.css">\r
<style>\r
  :root{ --bg:#fff; --card:#fff; --line:#e5e7eb; --ink:#111827; --muted:#6b7280; --accent:#2563eb; }\r
  *{box-sizing:border-box}\r
  body{\r
    margin:0;background:var(--bg);font:14px/1.5 system-ui,-apple-system,Segoe UI,Roboto,Helvetica,Arial;\r
    min-height:100vh;display:flex;flex-direction:column;align-items:stretch;\r
  }\r
  body.embedded{\r
    background:transparent;\r
    min-height:unset;\r
    display:block;\r
    padding:28px;\r
  }\r
  .auth-wrap{\r
    flex:1;\r
    width:100%;\r
    display:flex;\r
    align-items:center;\r
    justify-content:center;\r
    padding:24px 0;\r
  }\r
  body.embedded .auth-wrap{\r
    display:block;\r
    padding:0;\r
  }\r
  .card{\r
    width:360px;background:var(--card);border:1px solid rgba(0,0,0,.03);border-radius:18px;\r
    box-shadow:none;padding:28px 26px 26px;position:relative;\r
  }\r
  body.embedded .card{\r
    width:100%;\r
    max-width:none;\r
    border:none;\r
    box-shadow:none;\r
    padding:10px 8px 8px;\r
  }\r
  h1{ text-align:center;font-size:32px;margin:0 0 28px; }\r
  .tabs{ \r
    display:flex;\r
    gap:0;\r
    border-bottom:1px solid var(--line);\r
    margin-bottom:22px;\r
  }\r
  .tab{ \r
    flex:1;\r
    text-align:center;\r
    padding:14px 0;\r
    font-weight:600;\r
    cursor:pointer;\r
    background:transparent;\r
    color:var(--muted);\r
    font-size:15px;\r
    transition:all 0.2s ease;\r
    position:relative;\r
  }\r
  .tab:hover{\r
    color:var(--ink);\r
  }\r
  .tab.active{ \r
    background:transparent;\r
    color:var(--ink);\r
    border-bottom:2px solid var(--ink);\r
    margin-bottom:-1px;\r
  }\r
  .panels-container{\r
    position:relative;\r
    min-height:380px;\r
    display:flex;\r
    flex-direction:column;\r
  }\r
  #panel-login,\r
  #panel-register{\r
    position:absolute;\r
    top:0;\r
    left:0;\r
    right:0;\r
    bottom:0;\r
    opacity:0;\r
    visibility:hidden;\r
    pointer-events:none;\r
    display:flex;\r
    flex-direction:column;\r
    justify-content:space-between;\r
  }\r
  #panel-login.active,\r
  #panel-register.active{\r
    position:relative;\r
    opacity:1;\r
    visibility:visible;\r
    pointer-events:auto;\r
    min-height:380px;\r
  }\r
  #panel-login .login-form-section{\r
    flex:1;\r
    display:flex;\r
    flex-direction:column;\r
    justify-content:center;\r
  }\r
  #panel-login .login-social-section{\r
    padding-top:8px;\r
  }\r
  label{ display:block;font-size:14px;margin:0 0 6px;color:var(--ink); }\r
  input, select{ width:100%;height:40px;border:1px solid var(--line);border-radius:10px;padding:0 10px;font-size:13px;background:#fff; }\r
  input:-webkit-autofill,\r
  input:-webkit-autofill:focus{\r
    box-shadow:0 0 0 1000px #fff inset;\r
    -webkit-text-fill-color:var(--ink);\r
  }\r
  .input-wrap{ position:relative; }\r
  .input-wrap input{ padding-right:44px; }\r
  .toggle-password{\r
    position:absolute; right:8px; top:50%; transform:translateY(-50%);\r
    width:32px; height:32px; border-radius:999px; border:1px solid var(--line);\r
    background:#fff; display:inline-flex; align-items:center; justify-content:center;\r
    cursor:pointer; color:#6b7280; transition:background .15s ease, box-shadow .15s ease;\r
  }\r
  .toggle-password:hover{ background:#f3f4f6; box-shadow:none; }\r
  .toggle-password svg{ width:18px; height:18px; stroke:currentColor; }\r
  .toggle-password .eye-off{ display:none; }\r
  .toggle-password.is-visible .eye-on{ display:none; }\r
  .toggle-password.is-visible .eye-off{ display:block; }\r
  .mb-3{ margin-bottom:18px }\r
  .btn-row{ display:flex;gap:12px;margin-top:18px; }\r
  .btn{ \r
    flex:1;\r
    height:44px;\r
    border-radius:10px;\r
    border:1px solid #000;\r
    background:#111;\r
    color:#fff;\r
    font-size:16px;\r
    cursor:pointer;\r
    box-shadow:none !important;\r
    transition:transform 0.15s ease;\r
    outline:none;\r
  }\r
  .btn:hover{\r
    transform:translateY(-2px);\r
    box-shadow:none !important;\r
  }\r
  .btn:active{\r
    transform:translateY(0);\r
    box-shadow:none !important;\r
  }\r
  .btn:focus{\r
    outline:none;\r
    box-shadow:none !important;\r
  }\r
  .btn:focus-visible{\r
    outline:none;\r
    box-shadow:none !important;\r
  }\r
  .btn[disabled]{opacity:.6;cursor:not-allowed}\r
  .btn.light{ background:#fff;color:#000; }\r
  .link-btn{ background:none;border:none;color:#2563eb;font-size:13px;cursor:pointer;padding:0; }\r
  .error{ font-size:12px;color:#dc2626;margin-top:4px;display:none; }\r
  .success{\r
    display:none; position:relative; padding:10px 12px; border-radius:12px;\r
    border:1px solid rgba(59,130,246,.25); background:rgba(59,130,246,.08);\r
    color:#1f3bb3; font-size:13px; margin-bottom:12px;\r
  }\r
  .success::before,\r
  .success::after{\r
    content:none;\r
  }\r
  .success .success-title{ margin:0 0 4px;font-size:13px;font-weight:600; }\r
  .success .success-body{ display:flex;flex-direction:column;gap:2px; }\r
  .success .success-text{ margin:0;color:inherit;font-size:13px; }\r
  .success.is-error{ border-color:rgba(239,68,68,.4);background:rgba(239,68,68,.08);color:#b91c1c; }\r
  .small-row{ display:flex;justify-content:flex-end;margin:6px 0 18px; }\r
  .switch-row{ display:flex;justify-content:flex-end;margin:8px 0 12px; }\r
  .divider{display:flex;align-items:center;gap:10px;margin:20px 0 16px;color:#6b7280;font-size:12px;}\r
  #reset-panel{ position:fixed;inset:0;background:rgba(15,23,42,.28);display:none;align-items:center;justify-content:center;z-index:999; }\r
  #reset-box{ width:320px;background:#fff;border-radius:16px;box-shadow:none;padding:20px 18px 16px; }\r
  #reset-box h3{ margin:0 0 6px;font-size:16px; }\r
  #reset-box p{ margin:0 0 12px;font-size:13px;color:#6b7280; }\r
  #reset-msg{ font-size:12px;color:#1d4ed8;background:#eff6ff;border:1px solid #bfdbfe;padding:6px 8px;border-radius:8px;display:none;margin-bottom:10px; }\r
  .status-dialog{ position:fixed; inset:0; background:rgba(15,23,42,.35); display:flex; align-items:center; justify-content:center; opacity:0; pointer-events:none; transition:opacity .25s ease; z-index:1200; padding:24px; backdrop-filter:blur(2px); }\r
  .status-dialog.active{ opacity:1; pointer-events:auto; }\r
  .status-dialog__card{ width:min(380px,90vw); background:#fff; border-radius:22px; padding:34px 30px 26px; text-align:center; box-shadow:none; transform:translateY(24px); transition:transform .3s ease; }\r
  .status-dialog.active .status-dialog__card{ transform:translateY(0); }\r
  .status-dialog__icon{ width:70px;height:70px;border-radius:18px;margin:0 auto 18px;display:flex;align-items:center;justify-content:center;font-size:30px;font-weight:700;color:#fff;background:linear-gradient(135deg,#2563eb,#1d4ed8);box-shadow:none; }\r
  .status-dialog[data-mode="success"] .status-dialog__icon{ background:linear-gradient(135deg,#3b82f6,#1d4ed8);box-shadow:none; }\r
  .status-dialog__title{ font-size:22px;margin:0 0 8px;color:#0f172a; }\r
  .status-dialog__text{ margin:0 0 22px;color:#4b5563;font-size:14px; }\r
  .status-dialog__btn{ border:none;border-radius:999px;width:100%;max-width:220px;margin:0 auto;height:46px;background:linear-gradient(135deg,#2563eb,#1d4ed8);color:#fff;font-size:15px;font-weight:600;cursor:pointer;box-shadow:none; }\r
  .status-dialog__btn:active{ transform:translateY(1px); }\r
  .divider::before,.divider::after{content:"";flex:1;height:1px;background:#e5e7eb;}\r
  .form-alert{display:none;margin:10px 0;padding:10px 12px;border-radius:12px;border:1px solid rgba(47,93,255,.25);background:rgba(47,93,255,.08);color:#1f3bb3;font-size:13px;animation:fadeUp .2s ease both;}\r
  @keyframes fadeUp{from{opacity:0;transform:translateY(4px);}to{opacity:1;transform:none;}}\r
</style>\r
</head>\r
<body>\r
  <main class="auth-wrap">\r
    <div class="card">\r
<h1>Club Booking Portal</h1>\r
\r
    <div class="tabs">\r
      <div id="tab-login" class="tab active">Login</div>\r
      <div id="tab-register-user" class="tab">Register</div>\r
    </div>\r
\r
    <div class="panels-container">\r
    <!-- 鐧诲綍闈㈡澘 -->\r
    <div id="panel-login" class="active">\r
      <div class="login-form-section">\r
      <div id="login-success" class="success" role="status" aria-live="polite">\r
        <div class="success-body">\r
          <p class="success-title">Registration complete</p>\r
          <p class="success-text">You can log in now.</p>\r
        </div>\r
      </div>\r
      <div class="mb-3">\r
        <label for="email">Email</label>\r
        <input id="email" type="email" autocomplete="username" />\r
        <div id="login-email-err" class="error">Please enter a valid email address.</div>\r
      </div>\r
      <div class="mb-3">\r
        <label for="password">Password</label>\r
        <div class="input-wrap">\r
          <input id="password" type="password" autocomplete="current-password" />\r
          <button id="togglePassword" class="toggle-password" type="button" aria-label="Show password">\r
            <svg class="eye-on" viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">\r
              <path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7S1 12 1 12z"></path>\r
              <circle cx="12" cy="12" r="3"></circle>\r
            </svg>\r
            <svg class="eye-off" viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">\r
              <path d="M17.94 17.94A10.94 10.94 0 0 1 12 20c-7 0-11-8-11-8a21.56 21.56 0 0 1 5.06-6.06"></path>\r
              <path d="M9.9 4.24A10.94 10.94 0 0 1 12 4c7 0 11 8 11 8a21.4 21.4 0 0 1-4.46 5.19"></path>\r
              <path d="M14.12 14.12a3 3 0 0 1-4.24-4.24"></path>\r
              <path d="M1 1l22 22"></path>\r
            </svg>\r
          </button>\r
        </div>\r
        <div id="login-pass-err" class="error">Password is required.</div>\r
      </div>\r
      <div id="login-form-err" class="form-alert" role="alert"></div>\r
      <div class="small-row">\r
        <button id="btnForgot" class="link-btn" type="button">Forgot password?</button>\r
      </div>\r
      <div class="btn-row">\r
        <button id="loginBtn" class="btn" type="button">Login</button>\r
        <button id="btnGoRegister" class="btn light" type="button">Create account</button>\r
      </div>\r
      </div><!-- end login-form-section -->\r
\r
      <div class="login-social-section">\r
      <!-- Google 鐧诲綍 -->\r
      <div class="divider"><span>or</span></div>\r
      <div style="text-align:center;">\r
        <div id="g_id_onload"\r
             data-client_id="885940597719-mtcoo82k9bbksvuj786nsj7iombqtsli.apps.googleusercontent.com"\r
             data-context="signin"\r
             data-ux_mode="popup"\r
             data-callback="handleGoogleCredential"\r
             data-auto_prompt="false">\r
        </div>\r
        <div class="g_id_signin"\r
             data-type="standard"\r
             data-shape="rectangular"\r
             data-theme="outline"\r
             data-text="signin_with"\r
             data-size="large"\r
             data-logo_alignment="left">\r
        </div>\r
      </div>\r
      </div><!-- end login-social-section -->\r
    </div>\r
\r
    <!-- 娉ㄥ唽闈㈡澘 -->\r
    <div id="panel-register">\r
      <div id="reg-success" class="success" role="status" aria-live="polite">\r
        <div class="success-body">\r
          <p class="success-title" id="reg-success-title">Heads up</p>\r
          <p class="success-text" id="reg-success-text"></p>\r
        </div>\r
      </div>\r
      <div class="switch-row">\r
        <button id="clubSwitch" class="link-btn" type="button">Register as a club?</button>\r
      </div>\r
\r
      <div id="user-extra" class="mb-3">\r
        <label for="reg-username">Your name / display name</label>\r
        <input id="reg-username" type="text" />
        <div id="reg-username-err" class="error">Please enter your name.</div>\r
      </div>\r
\r
      <div class="mb-3">\r
        <label for="reg-email">Email</label>\r
        <input id="reg-email" type="email" autocomplete="email" />\r
        <div id="reg-email-err" class="error">Please enter a valid email address.</div>\r
      </div>\r
      <div class="mb-3">\r
        <label for="reg-pass">Password</label>\r
        <input id="reg-pass" type="password" autocomplete="new-password" />\r
        <div id="reg-pass-err" class="error">Password is required.</div>\r
      </div>\r
      <div class="mb-3">\r
        <label for="reg-pass2">Confirm password</label>\r
        <input id="reg-pass2" type="password" autocomplete="new-password" />
        <div id="reg-pass2-err" class="error">Passwords do not match.</div>\r
      </div>\r
      <div class="btn-row">\r
        <button id="btnRegisterDo" class="btn" type="button">Register</button>\r
        <button id="btnBackLogin" class="btn light" type="button">Back to login</button>\r
      </div>\r
    </div>\r
    </div><!-- end panels-container -->\r
    </div>\r
  </main>\r
\r
\r
  <!-- 閲嶇疆瀵嗙爜寮瑰眰 -->\r
  <div id="reset-panel">\r
    <div id="reset-box">\r
      <h3>Reset password</h3>\r
      <p>Enter your email address, we'll send you a reset link.</p >\r
      <div id="reset-msg">Email sent.</div>\r
      <div class="mb-3">\r
        <label for="reset-email">Email</label>\r
        <input id="reset-email" type="email" />\r
        <div id="reset-email-err" class="error">Please enter a valid email address.</div>\r
      </div>\r
      <div class="btn-row">\r
        <button id="btnResetSend" class="btn" type="button">Send link</button>\r
        <button id="btnResetBack" class="btn light" type="button">Back</button>\r
      </div>\r
    </div>\r
  </div>\r
\r
  <div id="status-dialog" class="status-dialog" aria-hidden="true" data-mode="success">\r
    <div class="status-dialog__card" role="alertdialog" aria-labelledby="status-dialog-title" aria-describedby="status-dialog-text">\r
      <div class="status-dialog__icon" id="status-dialog-icon" aria-hidden="true">&#10003;</div>\r
      <p id="status-dialog-title" class="status-dialog__title"></p>\r
      <p id="status-dialog-text" class="status-dialog__text"></p>\r
      <button type="button" id="status-dialog-btn" class="status-dialog__btn">OK</button>\r
    </div>\r
  </div>\r
\r
  <!-- Google 韬唤鏈嶅姟 SDK -->\r
  <script src="https://accounts.google.com/gsi/client" async defer><\/script>\r
\r
  <!-- 璇婃柇杈呭姪锛氳緭鍑?client_id 涓庢潵婧愶紝骞剁瓑寰?GSI 鍙敤 -->\r
  <script>\r
    (function(){\r
      try{\r
        const el = document.querySelector('[data-client_id]');\r
        const clientId = el ? el.getAttribute('data-client_id') : null;\r
        console.log('[GSI DEBUG] page origin =', location.origin);\r
        console.log('[GSI DEBUG] client_id attr =', clientId);\r
\r
        function checkGsi(){\r
          const ok = !!(window.google && window.google.accounts && window.google.accounts.id);\r
          console.log('[GSI DEBUG] google.accounts.id available =', ok);\r
          if(!ok){\r
            // 閲嶈瘯鍑犳\r
            return false;\r
          }\r
          return true;\r
        }\r
\r
        let attempts = 0;\r
        const maxAttempts = 8;\r
        const t = setInterval(()=>{\r
          attempts++;\r
          const ready = checkGsi();\r
          if(ready || attempts >= maxAttempts){\r
            clearInterval(t);\r
            if(!ready) console.warn('[GSI DEBUG] Google Identity Services did not become available after retries.');\r
          }\r
        }, 300);\r
      }catch(e){ console.error('[GSI DEBUG] diagnostic error', e); }\r
    })();\r
  <\/script>\r
\r
  <!-- 濡傛灉 SDK 鏈湴鍖栦簡鎸夐挳鏂囨锛屽皾璇曞己鍒舵敼鍥炶嫳鏂?-->\r
  <script>\r
    (function(){\r
      const desired = 'Sign in with Google';\r
      let attempts = 0;\r
      const maxAttempts = 12;\r
      const t = setInterval(()=>{\r
        attempts++;\r
        try{\r
          const container = document.querySelector('.g_id_signin');\r
          if(!container) return;\r
          // 鏌ユ壘鍖呭惈鏈湴鍖栨枃妗堢殑鎸夐挳鎴?span\r
          const btn = container.querySelector('button') || container.querySelector('[role="button"]') || container.querySelector('div');\r
          if(!btn) return;\r
          // 鏌ユ壘鎵胯浇鏂囨鐨勫厓绱?\r
          const textEl = btn.querySelector('span') || btn;\r
          if(textEl && textEl.textContent && /浣跨敤|鐧诲綍|鐧诲叆|甯宠櫉|璐﹀彿|Google/.test(textEl.textContent)){\r
            textEl.textContent = desired;\r
            // 濡傛湁 aria-label 鍒欏悓姝ユ洿鏂?\r
            if(btn.getAttribute){\r
              const aria = btn.getAttribute('aria-label');\r
              if(aria) btn.setAttribute('aria-label', desired);\r
            }\r
            clearInterval(t);\r
            return;\r
          }\r
          // 澶勭悊 SDK 鐢熸垚澶氬眰缁撴瀯鐨勬儏鍐?\r
          const allSpans = container.querySelectorAll('span');\r
          for(const s of allSpans){\r
            if(s.textContent && /浣跨敤|鐧诲綍|鐧诲叆|甯宠櫉|璐﹀彿|Google/.test(s.textContent)){\r
              s.textContent = desired;\r
              clearInterval(t);\r
              return;\r
            }\r
          }\r
        }catch(e){ /* 蹇界暐 */ }\r
        if(attempts >= maxAttempts) clearInterval(t);\r
      }, 250);\r
    })();\r
  <\/script>\r
  <script>\r
  const API_BASE = "http://13.40.74.21:8080/api";\r
  // TODO: Replace with real backend endpoints (user + club auth).\r
  const AUTH_ENDPOINTS = {\r
    userLogin: \`\${API_BASE}/login\`,\r
    clubLogin: \`\${API_BASE}/clubs/login\`,\r
    userRegister: \`\${API_BASE}/register\`,\r
    clubRegister: \`\${API_BASE}/clubs/register\`,\r
    googleLogin: \`\${API_BASE}/auth/google\`\r
  };\r
  const emailRe = /^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$/;\r
\r
  const tabLogin = document.getElementById('tab-login');\r
  const tabRegisterUser = document.getElementById('tab-register-user');\r
  const panelLogin = document.getElementById('panel-login');\r
  const panelRegister = document.getElementById('panel-register');\r
  const regSuccess = document.getElementById('reg-success');\r
  const regSuccessTitle = document.getElementById('reg-success-title');\r
  const regSuccessText = document.getElementById('reg-success-text');\r
  const loginFormErr = document.getElementById('login-form-err');\r
\r
  const userExtra = document.getElementById('user-extra');\r
  const clubSwitch = document.getElementById('clubSwitch');\r
\r
  const resetPanel = document.getElementById('reset-panel');\r
  const resetEmail = document.getElementById('reset-email');\r
  const resetEmailErr = document.getElementById('reset-email-err');\r
  const resetMsg = document.getElementById('reset-msg');\r
\r
  const statusDialog = document.getElementById('status-dialog');\r
  const statusDialogTitle = document.getElementById('status-dialog-title');\r
  const statusDialogText = document.getElementById('status-dialog-text');\r
  const statusDialogBtn = document.getElementById('status-dialog-btn');\r
  const statusDialogIcon = document.getElementById('status-dialog-icon');\r
  let statusDialogCb = null;\r
\r
  const togglePasswordBtn = document.getElementById('togglePassword');\r
  const passwordInput = document.getElementById('password');\r
\r
  const showLoginError = (msg) => {\r
    if (!loginFormErr) return;\r
    loginFormErr.textContent = msg;\r
    loginFormErr.style.display = 'block';\r
    requestResize();\r
  };\r
  const hideLoginError = () => {\r
    if (loginFormErr) loginFormErr.style.display = 'none';\r
    requestResize();\r
  };\r
\r
  if (togglePasswordBtn && passwordInput) {\r
    togglePasswordBtn.addEventListener('click', () => {\r
      const isVisible = passwordInput.type === 'text';\r
      passwordInput.type = isVisible ? 'password' : 'text';\r
      togglePasswordBtn.classList.toggle('is-visible', !isVisible);\r
      togglePasswordBtn.setAttribute('aria-label', isVisible ? 'Show password' : 'Hide password');\r
    });\r
  }\r
\r
  function showStatusDialog({ title = '', message = '', buttonText = 'OK', icon = 'success', onConfirm = null } = {}){\r
    if (!statusDialog) return;\r
    statusDialogTitle.textContent = title;\r
    statusDialogText.textContent = message;\r
    statusDialogBtn.textContent = buttonText;\r
    statusDialogIcon.innerHTML = icon === 'success' ? '&#10003;' : '&#9432;';\r
    statusDialog.dataset.mode = icon;\r
    statusDialog.classList.add('active');\r
    statusDialog.setAttribute('aria-hidden','false');\r
    statusDialogCb = typeof onConfirm === 'function' ? onConfirm : null;\r
  }\r
  function hideStatusDialog(){\r
    if (!statusDialog) return;\r
    statusDialog.classList.remove('active');\r
    statusDialog.setAttribute('aria-hidden','true');\r
    if(statusDialogCb){ const cb = statusDialogCb; statusDialogCb = null; cb(); }\r
  }\r
  statusDialogBtn?.addEventListener('click', hideStatusDialog);\r
  statusDialog?.addEventListener('click', (evt)=>{ if(evt.target === statusDialog){ hideStatusDialog(); } });\r
  document.addEventListener('keydown', (evt)=>{ if(evt.key === 'Escape' && statusDialog?.classList.contains('active')){ hideStatusDialog(); } });\r
\r
  function setRegNotice(title, message, isError = false){\r
    if (!regSuccess) return;\r
    regSuccessTitle.textContent = title;\r
    regSuccessText.textContent = message;\r
    regSuccess.classList.toggle('is-error', !!isError);\r
    regSuccess.style.display = 'block';\r
    requestResize();\r
  }\r
  function hideRegNotice(){\r
    if (!regSuccess) return;\r
    regSuccess.style.display = 'none';\r
    regSuccess.classList.remove('is-error');\r
    regSuccessText.textContent = '';\r
    requestResize();\r
  }\r
\r
  function switchToLogin(showSuccess){\r
    tabLogin?.classList.add('active');\r
    tabRegisterUser?.classList.remove('active');\r
    panelLogin?.classList.add('active');\r
    panelRegister?.classList.remove('active');\r
    if (resetPanel) resetPanel.style.display = 'none';\r
    const succLogin = document.getElementById('login-success');\r
    succLogin.style.display = showSuccess ? 'block' : 'none';\r
    if(showSuccess){ setTimeout(()=>{ succLogin.style.display = 'none'; }, 2200); }\r
    requestResize();\r
  }\r
\r
  function switchToRegister(){\r
    tabLogin?.classList.remove('active');\r
    tabRegisterUser?.classList.add('active');\r
    panelRegister?.classList.add('active');\r
    panelLogin?.classList.remove('active');\r
    if (resetPanel) resetPanel.style.display = 'none';\r
    hideRegNotice();\r
    if (userExtra) userExtra.style.display = 'block';\r
    requestResize();\r
  }\r
\r
  tabLogin?.addEventListener('click', ()=>switchToLogin(false));\r
  tabRegisterUser?.addEventListener('click', ()=>switchToRegister());\r
  document.getElementById('btnGoRegister')?.addEventListener('click', ()=>switchToRegister());\r
  document.getElementById('email')?.addEventListener('input', hideLoginError);\r
  document.getElementById('password')?.addEventListener('input', hideLoginError);\r
  document.getElementById('btnBackLogin')?.addEventListener('click', ()=>switchToLogin(false));\r
  const openClubRegister = () => {\r
    const target = 'club register.html';\r
    if (window.self !== window.top) {\r
      window.top.location.href = target;\r
      return;\r
    }\r
    window.location.href = target;\r
  };\r
  clubSwitch?.addEventListener('click', openClubRegister);\r
\r
  function consumePostLoginRedirect(){\r
    try{\r
      const raw = localStorage.getItem('postLoginRedirect');\r
      if (!raw) return null;\r
      localStorage.removeItem('postLoginRedirect');\r
      const data = JSON.parse(raw);\r
      if (data && data.page === 'join' && data.clubId) {\r
        return \`join.html?club=\${encodeURIComponent(data.clubId)}\`;\r
      }\r
    }catch(e){\r
      try{ localStorage.removeItem('postLoginRedirect'); }catch(err){}\r
    }\r
    return null;\r
  }\r
\r
  const isEmbedded = window.self !== window.top;\r
  if (isEmbedded) document.body.classList.add('embedded');\r
\r
  const notifyParentSize = () => {\r
    if (!isEmbedded) return;\r
    const height = Math.max(\r
      document.body?.scrollHeight || 0,\r
      document.documentElement?.scrollHeight || 0\r
    );\r
    window.parent.postMessage({ type: 'auth-resize', height }, '*');\r
  };\r
\r
  const requestResize = () => {\r
    if (!isEmbedded) return;\r
    requestAnimationFrame(() => notifyParentSize());\r
  };\r
\r
  if (isEmbedded) {\r
    window.addEventListener('load', notifyParentSize);\r
    window.addEventListener('resize', notifyParentSize);\r
    if (window.ResizeObserver) {\r
      const ro = new ResizeObserver(() => notifyParentSize());\r
      ro.observe(document.body);\r
    }\r
    setTimeout(notifyParentSize, 50);\r
  }\r
\r
  function resolveUserType(role) {\r
    const r = String(role || '').toLowerCase();\r
    if (!r) return 'user';\r
    if (r.includes('club') || r.includes('admin') || r.includes('leader')) return 'club';\r
    return 'user';\r
  }\r
\r
  function getDefaultTarget(role) {\r
    if (role) return resolveUserType(role) === 'club' ? 'club home.html' : 'home.html';\r
    try {\r
      const logged = JSON.parse(localStorage.getItem('loggedUser') || 'null');\r
      if (logged && logged.type === 'club') return 'club home.html';\r
    } catch (e) { /* ignore */ }\r
    return 'home.html';\r
  }\r
\r
  function redirectAfterLogin(role){\r
    const userType = resolveUserType(role);\r
    const target = userType === 'club'\r
      ? getDefaultTarget(role)\r
      : (consumePostLoginRedirect() || getDefaultTarget(role));\r
    if (isEmbedded) {\r
      window.parent.postMessage({ type: 'auth-success', target }, '*');\r
      return;\r
    }\r
    window.location.href = target;\r
  }\r
\r
  async function handleLogin(){\r
    const email = document.getElementById('email').value.trim();\r
    const pass  = document.getElementById('password').value;\r
\r
    hideLoginError();\r
    document.getElementById('login-email-err').style.display = 'none';\r
    document.getElementById('login-pass-err').style.display = 'none';\r
    if(!emailRe.test(email)){\r
      showLoginError('Invalid email format');\r
      return;\r
    }\r
    if(!pass){\r
      showLoginError('Password is required');\r
      return;\r
    }\r
\r
    const btn = document.getElementById('loginBtn');\r
    btn.disabled = true; btn.textContent = 'Logging in...';\r
    try {\r
      const res = await fetch(AUTH_ENDPOINTS.userLogin, {\r
        method: 'POST',\r
        headers: { 'Content-Type': 'application/json' },\r
        body: JSON.stringify({ email, password: pass })\r
      });\r
      if (res.ok) {\r
        const data = await res.json();\r
        localStorage.setItem('user', JSON.stringify(data));\r
        const role = data.role || data.type;\r
        const userType = resolveUserType(role);\r
        localStorage.setItem('loggedUser', JSON.stringify({\r
          email: data.email,\r
          name: data.fullName,\r
          type: userType\r
        }));\r
        redirectAfterLogin(role);\r
      } else {\r
        const text = await res.text();\r
        const msg = (res.status == 401 || res.status == 400) ? 'Incorrect password' : (text || 'Login failed');\r
        showLoginError(msg);\r
      }\r
    } catch(err){\r
      showLoginError('Network error, please try again.');\r
    } finally {\r
      btn.disabled = false; btn.textContent = 'Login';\r
    }\r
  }\r
\r
  window.handleGoogleCredential = async function (response) {\r
    try {\r
      const idToken = response && response.credential;\r
      if (!idToken) {\r
        alert('Google sign-in failed: no credential received');\r
        return;\r
      }\r
      const res = await fetch(AUTH_ENDPOINTS.googleLogin, {\r
        method: 'POST',\r
        headers: { 'Content-Type': 'application/json' },\r
        body: JSON.stringify({ credential: idToken })\r
      });\r
\r
      if (!res.ok) {\r
        const t = await res.text();\r
        alert('Google sign-in failed: ' + (t || res.status));\r
        return;\r
      }\r
      const user = await res.json();\r
      localStorage.setItem('user', JSON.stringify(user));\r
      const role = user.role || user.type;\r
      const userType = resolveUserType(role);\r
      localStorage.setItem('loggedUser', JSON.stringify({\r
        email: user.email,\r
        name: user.fullName,\r
        type: userType\r
      }));\r
      redirectAfterLogin(role);\r
    } catch (e) {\r
      console.error(e);\r
      alert('Network error, please try again later');\r
    }\r
  };\r
\r
  async function handleRegister(){\r
    const username = document.getElementById('reg-username').value.trim();\r
    const email  = document.getElementById('reg-email').value.trim();\r
    const pass   = document.getElementById('reg-pass').value;\r
    const pass2  = document.getElementById('reg-pass2').value;\r
\r
    hideLoginError();\r
    let ok = true;\r
\r
    if (!username) { document.getElementById('reg-username-err').style.display = 'block'; ok = false; }\r
    else document.getElementById('reg-username-err').style.display = 'none';\r
\r
    if(!emailRe.test(email)){ document.getElementById('reg-email-err').style.display = 'block'; ok = false; }\r
    else document.getElementById('reg-email-err').style.display = 'none';\r
    if(!pass){ document.getElementById('reg-pass-err').style.display = 'block'; ok = false; }\r
    else document.getElementById('reg-pass-err').style.display = 'none';\r
    if(pass !== pass2){ document.getElementById('reg-pass2-err').style.display = 'block'; ok = false; }\r
    else document.getElementById('reg-pass2-err').style.display = 'none';\r
    if(!ok) return;\r
\r
    let fullName = username || email.split('@')[0];\r
\r
    const btn = document.getElementById('btnRegisterDo');\r
    btn.disabled = true; btn.textContent = 'Registering...';\r
    hideRegNotice();\r
    try {\r
      const res = await fetch(AUTH_ENDPOINTS.userRegister, {\r
        method: 'POST',\r
        headers: { 'Content-Type': 'application/json' },\r
        body: JSON.stringify({ fullName, email, password: pass })\r
      });\r
\r
      if (res.ok) {\r
        let saved = null;\r
        try { saved = await res.json(); } catch (e) { saved = null; }\r
        const profileToSave = saved || { fullName, email, role: 'user' };\r
        localStorage.setItem('user', JSON.stringify(profileToSave));\r
        const role = profileToSave.role || profileToSave.type || 'user';\r
        const userType = resolveUserType(role);\r
        localStorage.setItem('loggedUser', JSON.stringify({\r
          email: profileToSave.email || email,\r
          name: profileToSave.fullName || fullName,\r
          type: userType\r
        }));\r
        setRegNotice('Success', 'Registered successfully! Redirecting to home...');\r
        setTimeout(()=> {\r
          if (isEmbedded) {\r
            window.parent.postMessage({ type: 'auth-success', target: getDefaultTarget(role) }, '*');\r
            return;\r
          }\r
          window.location.href = getDefaultTarget(role);\r
        }, 800);\r
      } else if (res.status === 409) {\r
        setRegNotice('Heads up', 'Email already exists.', true);\r
      } else {\r
        const t = await res.text();\r
        setRegNotice('Registration failed', t || 'Registration failed. Please try again.', true);\r
      }\r
    } catch(err) {\r
      setRegNotice('Network error', 'Please ensure the backend is running.', true);\r
    } finally {\r
      btn.disabled = false; btn.textContent = 'Register';\r
    }\r
  }\r
\r
  document.getElementById('btnForgot')?.addEventListener('click', function(){\r
    resetPanel.style.display = 'flex';\r
    resetEmail.value = ''; resetEmailErr.style.display = 'none'; resetMsg.style.display = 'none';\r
    requestResize();\r
  });\r
  document.getElementById('btnResetBack')?.addEventListener('click', function(){\r
    resetPanel.style.display = 'none';\r
    requestResize();\r
  });\r
  document.getElementById('btnResetSend')?.addEventListener('click', function(){\r
    const em = resetEmail.value.trim();\r
    if(!emailRe.test(em)){ resetEmailErr.style.display = 'block'; return; }\r
    resetEmailErr.style.display = 'none';\r
    resetMsg.textContent = 'Reset link sent to: ' + em;\r
    resetMsg.style.display = 'block';\r
  });\r
\r
  document.getElementById('loginBtn')?.addEventListener('click', handleLogin);\r
  document.getElementById('btnRegisterDo')?.addEventListener('click', handleRegister);\r
\r
  if(location.hash === '#register-club'){ openClubRegister(); }\r
  else if(location.hash === '#register' || location.hash === '#register-user'){ switchToRegister(); }\r
<\/script>\r
</body>\r
</html>\r
`,Dc=`\uFEFF\uFEFF<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <title>Club onboarding - Location details</title>
  <link rel="stylesheet" href="theme.css">
  <link rel="stylesheet" href="onboarding.css">
</head>
<body>
  <main class="wrap">
    <a class="link-back back-pill" href="onboarding.html">Back to profile</a>
    <header class="hero">
      <span class="hero-kicker">Club setup</span>
      <h1>Location details</h1>
      <p class="lead">Choose where your club operates and how many venues you manage.</p>
    </header>

    <section class="panel">
      <div class="panel-header">
        <div>
          <h2>Club location</h2>
          <p>Placeholders only.</p>
        </div>
        <span class="panel-badge">Step 2 of 2</span>
      </div>

      <div class="form-block">
        <label for="clubLocation" class="label">Club location</label>
        <input id="clubLocationInput" type="text" placeholder="Type a UK city (placeholder)">
        <div class="map-placeholder" aria-hidden="true">
          <div class="map-placeholder__badge">Map placeholder</div>
          <div class="map-placeholder__body">
            Google Maps integration goes here.
          </div>
        </div>
        <div class="field-hint">This will be shown on your public club page.</div>
      </div>

      <div class="form-block">
        <label for="clubVenues" class="label">Number of courts / venues</label>
        <div class="field-hint">Add each venue and describe it (placeholder only).</div>
        <div id="venueList" class="venue-list" aria-label="Venue list"></div>
        <button id="addVenue" class="btn ghost venue-add" type="button">Add venue</button>
      </div>

      <div class="actions">
        <button id="btnBack" class="btn ghost" type="button">Back</button>
        <button id="btnFinish" class="btn primary" type="button">Finish setup</button>
      </div>
    </section>
  </main>

  <footer class="site-footer">
    <div class="site-footer__wrap">
      <span class="site-footer__text">&copy; 2026 Club Booking Portal</span>
      <div class="site-footer__actions">
        <button class="site-footer__btn info-trigger" type="button" data-info="privacy">Privacy</button>
        <button class="site-footer__btn info-trigger" type="button" data-info="terms">Terms</button>
        <button class="site-footer__btn info-trigger" type="button" data-info="help">Help</button>
      </div>
    </div>
  </footer>

  <script src="onboarding-location.js" defer><\/script>
  <script src="auth-modal.js" defer><\/script>
</body>
</html>
`,Uc=`\uFEFF\uFEFF<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <title>Club onboarding - Set up your club profile</title>
  <link rel="stylesheet" href="theme.css">
  <link rel="stylesheet" href="onboarding.css">
</head>
<body>
  <header style="position: sticky; top: 0; z-index: 10; border-bottom: 1px solid #e5e7eb; background: white; padding: 12px 0;">
    <div class="wrap" style="max-width: 1100px; margin: 0 auto; padding: 0 16px; display: flex; align-items: center; justify-content: space-between; gap: 16px;">
      <div style="display: flex; align-items: center; gap: 16px;">
        <button onclick="window.parent.location.href = '/club-home'" type="button" style="border: none; background: none; cursor: pointer; font-size: 14px; color: #0066cc; text-decoration: none; padding: 8px 0;">← Back</button>
        <h2 style="margin: 0; font-size: 18px; font-weight: 600;">Club profile</h2>
      </div>
    </div>
  </header>
  <main class="wrap">
    <header class="hero">
      <span class="hero-kicker">Club setup</span>
      <h1>Set up your club profile</h1>
      <p class="lead">Add your club name and choose the sports your club offers so members can find you.</p>
    </header>

    <section class="panel">
      <div class="panel-header">
        <div>
          <h2>Profile details</h2>
          <p>Finish in under a minute.</p>
        </div>
        <span class="panel-badge">Step 1 of 2</span>
      </div>

      <div class="form-block">
        <label for="displayName" class="label">Club name</label>
        <input id="displayName" type="text" placeholder="The name members will see" autocomplete="name">
        <div class="field-hint">This appears on your public club page.</div>
      </div>

      <div class="form-block">
        <div class="label">Club type</div>
        <div class="field-hint">Pick any sports your club offers.</div>
        <div id="sportsGrid" class="sports-grid" aria-label="Choose sports your club offers" role="list">
          <!-- chips injected by onboarding.js -->
        </div>
        <div class="custom-type-block">
          <div class="custom-type-title">Add your own type</div>
          <div id="customTypeList" class="custom-type-list" aria-label="Add custom club types"></div>
          <button id="addCustomType" class="btn ghost custom-type-add" type="button">Add another</button>
        </div>
      </div>

      <div class="actions">
        <button id="btnSave" class="btn primary" type="button" disabled>Save and continue</button>
      </div>

      <div id="status" class="status" aria-live="polite"></div>
    </section>

  </main>

  <footer class="site-footer">
    <div class="site-footer__wrap">
      <span class="site-footer__text">&copy; 2026 Club Booking Portal</span>
      <div class="site-footer__actions">
        <button class="site-footer__btn info-trigger" type="button" data-info="privacy">Privacy</button>
        <button class="site-footer__btn info-trigger" type="button" data-info="terms">Terms</button>
        <button class="site-footer__btn info-trigger" type="button" data-info="help">Help</button>
      </div>
    </div>
  </footer>

  <script src="onboarding.js" defer><\/script>
  <script src="auth-modal.js" defer><\/script>
</body>
</html>
`,jc=`<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <title>Club Booking Portal - User Profile</title>
  <link rel="stylesheet" href="theme.css">
  <style>
    body { margin: 0; background: #fff; }
    header { position: sticky; top: 0; z-index: 10; border-bottom: none; }
    .wrap { max-width: 1100px; margin: 0 auto; padding: 16px; }
    .header-wrap { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
    .site-title-link { color: inherit; text-decoration: none; }
    .page-head { display: none; }
    .page-actions { display: none; }

    .profile-shell {
      display: grid;
      grid-template-columns: 220px minmax(0, 1fr);
      gap: 24px;
      margin-top: 0;
      padding: 8px 0 0;
      background: transparent;
      border-radius: 0;
    }

    .profile-card {
      padding: 0;
    }

    .side-nav {
      position: sticky;
      top: 96px;
      align-self: start;
      display: flex;
      flex-direction: column;
      gap: 8px;
      padding: 0 18px 0 0;
      border-right: none;
      background: none;
      box-shadow: none;
    }

    .side-title {
      font-size: 18px;
      font-weight: 700;
      padding: 6px 10px 2px;
      color: var(--ink);
    }

    .side-link {
      width: 100%;
      border: none;
      background: none;
      text-align: left;
      padding: 10px 12px;
      border-radius: 12px;
      font-weight: 600;
      cursor: pointer;
      color: var(--muted);
      transition: color .2s var(--anim-base), background .2s var(--anim-base), border-color .2s var(--anim-base), box-shadow .2s var(--anim-base);
    }

    .side-link:hover {
      background: rgba(21, 23, 26, 0.04);
      color: var(--ink);
    }

    .side-link.active {
      color: var(--accent-strong);
      background: rgba(47, 93, 255, 0.1);
      border-color: transparent;
      box-shadow: none;
    }

    .side-subnav {
      display: none;
      flex-direction: column;
      gap: 6px;
      margin-top: auto;
      padding-top: 6px;
    }

    .side-nav.info-active .side-subnav {
      display: flex;
    }

    .side-sublink {
      width: fit-content;
      border: none;
      background: none;
      text-align: left;
      padding: 6px 10px;
      border-radius: 999px;
      font-size: 12px;
      font-weight: 600;
      color: var(--muted);
      cursor: pointer;
    }

    .side-sublink.active {
      color: var(--accent-strong);
      background: rgba(47, 93, 255, 0.1);
    }

    .panel {
      display: none;
    }

    .panel.active {
      display: block;
    }

    .profile-shell .pad { padding: 6px 6px 12px; }
    .card .pad { padding: 16px; }
    .booking-list { display: grid; gap: 12px; margin-top: 12px; }
    .booking-card {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
      padding: 14px 0;
      border: none;
      border-bottom: none;
      border-radius: 0;
      background: transparent;
      box-shadow: none;
      transition: transform .25s var(--anim-base), box-shadow .25s var(--anim-base);
    }
    .booking-card:last-child {
      border-bottom: none;
    }
    .booking-meta { display: grid; gap: 4px; }
    .booking-title { font-weight: 700; font-size: 15px; }
    .booking-sub { color: var(--muted); font-size: 13px; }
    .booking-actions { display: flex; gap: 8px; flex-wrap: wrap; }
    .btn {
      border-radius: 999px;
      padding: 8px 16px;
      font-size: 13px;
      text-decoration: none;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      box-shadow: none;
    }
    .btn:hover {
      box-shadow: none;
    }
    .btn.small { padding: 6px 10px; font-size: 12px; }

    .info-grid { display: grid; gap: 16px; }
    .info-tabs { display: none; }
    .info-tab {
      flex: 1;
      background: none;
      border: none;
      padding: 12px 0;
      font-weight: 600;
      cursor: pointer;
      color: var(--muted);
      border-bottom: none;
    }
    .info-tab.active { color: var(--accent-strong); }
    .info-section { display: none; }
    .info-section.active { display: block; }
    .profile-hero {
      display: flex;
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
      margin-bottom: 16px;
    }
    .avatar-uploader {
      position: relative;
      width: 96px;
      height: 96px;
      border-radius: 50%;
      background: #eef1f6;
      overflow: hidden;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      flex-shrink: 0;
    }

    .avatar-uploader input {
      position: absolute;
      inset: 0;
      opacity: 0;
      cursor: pointer;
    }

    .avatar-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }

    .avatar-placeholder {
      font-size: 18px;
      font-weight: 700;
      color: rgba(15, 23, 42, 0.5);
    }

    .avatar-badge {
      position: absolute;
      right: 6px;
      bottom: 6px;
      width: 28px;
      height: 28px;
      border-radius: 50%;
      background: rgba(15, 23, 42, 0.9);
      display: inline-flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      pointer-events: none;
    }

    .profile-name {
      font-size: 18px;
      font-weight: 700;
    }

    .profile-email {
      color: var(--muted);
      font-size: 13px;
    }
    .form-field { margin-bottom: 0; }
    .form-field label { font-size: 13px; color: var(--muted); }
    .form-field input {
      width: 100%;
      height: 36px;
      border-radius: 10px;
      border: 1.5px solid #111;
      background: transparent;
      padding: 0 12px;
      font-size: 14px;
    }
    .row-field {
      display: flex;
      align-items: center;
      justify-content: flex-start;
      gap: 12px;
      padding: 8px 0;
      flex-wrap: wrap;
    }
    .row-field label {
      min-width: 60px;
    }
    .input-wrap {
      position: relative;
      display: inline-flex;
      align-items: center;
      flex: 1 1 260px;
      min-width: 220px;
    }
    .row-field input {
      width: 100%;
      text-align: left;
      padding-right: 40px;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .row-field input[readonly] {
      background: #f5f6fb;
      color: var(--ink);
    }
    .row-actions {
      display: inline-flex;
      gap: 8px;
      align-items: center;
    }
    .edit-icon {
      position: absolute;
      right: 6px;
      top: 50%;
      transform: translateY(-50%);
      width: 28px;
      height: 28px;
      border-radius: 50%;
      border: none;
      background: #111;
      color: #fff;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
    }
    .confirm-inline {
      display: none;
      align-items: center;
      gap: 8px;
      color: var(--muted);
      font-size: 12px;
    }
    .confirm-inline.open {
      display: inline-flex;
    }
    .name-row .row-actions {
      display: none;
    }
    .name-row.editing .row-actions {
      display: inline-flex;
    }
    .form-actions { display: flex; gap: 8px; margin-top: 18px; }
    .error-text { color: var(--danger); font-size: 12px; margin-top: 4px; display: none; }

    .modal-overlay {
      position: fixed;
      inset: 0;
      display: none;
      align-items: center;
      justify-content: center;
      padding: 24px;
      background: rgba(10, 12, 16, 0.45);
      z-index: 3500;
    }
    .modal-overlay.open { display: flex; }
    .modal-card {
      position: relative;
      width: min(520px, 92vw);
      background: #fff;
      border-radius: 18px;
      box-shadow: 0 24px 60px rgba(10, 12, 16, 0.25);
      padding: 22px 22px 18px;
    }
    .modal-card h3 { margin: 0 0 10px; font-size: 18px; }
    .modal-card p { margin: 0 0 14px; color: var(--muted); font-size: 13px; }
    .modal-close {
      position: absolute;
      top: 10px;
      right: 10px;
      width: 30px;
      height: 30px;
      border-radius: 50%;
      border: none;
      background: #fff;
      cursor: pointer;
      color: var(--muted);
    }
    .modal-field {
      display: grid;
      gap: 6px;
      margin-bottom: 12px;
    }
    .modal-field label { font-size: 12px; color: var(--muted); }
    .modal-field input {
      height: 36px;
      border-radius: 10px;
      border: 1.5px solid #111;
      padding: 0 10px;
      font-size: 14px;
    }
    .code-row {
      display: grid;
      grid-template-columns: 1fr auto;
      gap: 8px;
      align-items: center;
    }
    .modal-actions {
      display: flex;
      gap: 8px;
      justify-content: flex-end;
      margin-top: 10px;
    }

    .info-overlay {
      position: fixed;
      inset: 0;
      display: none;
      align-items: center;
      justify-content: center;
      padding: 24px;
      background: rgba(10, 12, 16, 0.45);
      z-index: 3000;
    }
    .info-overlay.open { display: flex; }
    .info-card {
      position: relative;
      width: min(560px, 92vw);
      background: #fff;
      border-radius: 16px;
      box-shadow: 0 24px 60px rgba(10, 12, 16, 0.25);
      padding: 20px 20px 18px;
    }
    .info-card h3 { margin: 0 0 8px; font-size: 18px; }
    .info-body p { margin: 0 0 10px; color: var(--muted); font-size: 13px; }
    .info-close {
      position: absolute;
      top: 10px;
      right: 10px;
      width: 30px;
      height: 30px;
      border-radius: 999px;
      border: none;
      background: #fff;
      cursor: pointer;
      color: var(--muted);
    }

    @media (max-width: 960px) {
      .profile-shell { grid-template-columns: 1fr; }
      .side-nav {
        position: static;
        flex-direction: column;
        border-right: none;
        border-bottom: none;
        padding: 0 0 12px;
      }
      .side-link { text-align: left; }
      .side-subnav {
        flex-direction: column;
        width: auto;
        justify-content: flex-start;
        margin-top: 6px;
      }
    }

    @media (max-width: 720px) {
      .booking-card { flex-direction: column; align-items: flex-start; }
      .booking-actions { width: 100%; }
      .header-wrap { flex-wrap: wrap; }
    }
  </style>
</head>
<body>
  <header>
    <div class="wrap header-wrap">
      <div style="display:flex;align-items:center;gap:12px;">
        <a class="link-back" href="home.html">Back to Home</a>
        <h1 class="site-title"><a class="site-title-link" href="home.html">Club Booking Portal</a></h1>
      </div>
    </div>
  </header>

  <main class="wrap">
    <section class="profile-card">
      <div class="profile-shell">
        <aside class="side-nav" aria-label="Profile Sections">
        <div class="side-title">User Profile</div>
        <button class="side-link active" type="button" data-panel="bookings">My Bookings</button>
        <button class="side-link" type="button" data-panel="info">Information</button>
        <div class="side-subnav" aria-label="Information Tabs">
          <button id="tabProfile" class="side-sublink active" type="button">Profile</button>
          <button id="tabSecurity" class="side-sublink" type="button">Security</button>
        </div>
      </aside>

        <section class="panel active" id="panelBookings" data-panel="bookings">
          <div class="pad">
            <div class="booking-list">
              <div class="booking-card">
                <div class="booking-meta">
                  <div class="booking-title">Badminton Club</div>
                  <div class="booking-sub">Friday 18:00 Training</div>
                </div>
                <div class="booking-actions">
                  <button class="btn ghost small" type="button">Details</button>
                  <button class="btn red small" type="button">Cancel</button>
                </div>
              </div>
              <div class="booking-card">
                <div class="booking-meta">
                  <div class="booking-title">Yoga Club</div>
                  <div class="booking-sub">Saturday 10:00 Class</div>
                </div>
                <div class="booking-actions">
                  <button class="btn ghost small" type="button">Details</button>
                  <button class="btn red small" type="button">Cancel</button>
                </div>
              </div>
              <div class="booking-card">
                <div class="booking-meta">
                  <div class="booking-title">Basketball Club</div>
                  <div class="booking-sub">Sunday 15:00 Match</div>
                </div>
                <div class="booking-actions">
                  <button class="btn ghost small" type="button">Details</button>
                  <button class="btn red small" type="button">Cancel</button>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section class="panel" id="panelInfo" data-panel="info">
          <div class="pad">
            <div id="profileTab" class="info-section active">
              <h4 style="margin:0 0 12px;font-size:16px">Personal Information</h4>
              <div class="profile-hero">
                <label class="avatar-uploader" aria-label="Upload avatar">
                  <span class="avatar-placeholder" id="avatarPlaceholder">+</span>
                  <img id="avatarImg" class="avatar-img" alt="Avatar" style="display:none" />
                  <span class="avatar-badge" aria-hidden="true">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                      <path d="M4 8.5C4 7.12 5.12 6 6.5 6H8.7L9.7 4.5C10.05 3.97 10.64 3.65 11.27 3.65H12.73C13.36 3.65 13.95 3.97 14.3 4.5L15.3 6H17.5C18.88 6 20 7.12 20 8.5V17.5C20 18.88 18.88 20 17.5 20H6.5C5.12 20 4 18.88 4 17.5V8.5Z" stroke="#fff" stroke-width="1.6"/>
                      <circle cx="12" cy="13" r="3.3" stroke="#fff" stroke-width="1.6"/>
                    </svg>
                  </span>
                  <input id="avatarInput" type="file" accept="image/*" />
                </label>
              </div>

              <div class="form-field row-field name-row" id="nameRow">
                <label for="modalNameInput">Name</label>
                <div class="input-wrap">
                  <input id="modalNameInput" type="text" readonly />
                  <button id="editNameBtn" class="edit-icon" type="button" aria-label="Edit name">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                      <path d="M4 20h4l10-10a2 2 0 0 0-4-4L4 16v4Z" stroke="#fff" stroke-width="1.6" stroke-linejoin="round"/>
                    </svg>
                  </button>
                </div>
                <div class="row-actions">
                  <button id="saveNameBtn" class="btn small" type="button" style="display:none">Save</button>
                  <button id="cancelNameBtn" class="btn ghost small" type="button" style="display:none">Cancel</button>
                  <div id="nameConfirm" class="confirm-inline" role="status" aria-live="polite">
                    Confirm save?
                    <button id="confirmNameBtn" class="btn small" type="button">Confirm</button>
                    <button id="dismissNameBtn" class="btn ghost small" type="button">Cancel</button>
                  </div>
                </div>
              </div>
              <div class="form-field row-field">
                <label for="modalEmailInput">Email</label>
                <div class="input-wrap">
                  <input id="modalEmailInput" type="text" readonly />
                  <button id="editEmailBtn" class="edit-icon" type="button" aria-label="Edit email">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                      <path d="M4 20h4l10-10a2 2 0 0 0-4-4L4 16v4Z" stroke="#fff" stroke-width="1.6" stroke-linejoin="round"/>
                    </svg>
                  </button>
                </div>
              </div>

            </div>

            <div id="securityTab" class="info-section">
              <h4 style="margin:0 0 12px;font-size:16px">Change Password</h4>

              <div class="form-field">
                <label for="currentPassInput">Current Password</label>
                <input id="currentPassInput" type="password" />
                <div id="currentPassErr" class="error-text"></div>
              </div>

              <div class="form-field">
                <label for="newPassInput">New Password</label>
                <input id="newPassInput" type="password" />
                <div id="newPassErr" class="error-text"></div>
              </div>

              <div class="form-field">
                <label for="confirmPassInput">Confirm New Password</label>
                <input id="confirmPassInput" type="password" />
                <div id="confirmPassErr" class="error-text"></div>
              </div>

              <p style="color:var(--muted);font-size:13px;margin:12px 0 0">Passwords are stored using bcrypt hashing.</p>

              <div class="form-actions">
                <button id="updatePassBtn" class="btn blue" type="button" style="flex:1">Update Password</button>
              </div>
            </div>
          </div>
        </section>
      </div>
    </section>
  </main>

  <footer class="site-footer">
    <div class="site-footer__wrap">
      <span class="site-footer__text">&copy; 2026 Club Booking Portal</span>
      <div class="site-footer__actions">
        <button class="site-footer__btn info-trigger" type="button" data-info="privacy">Privacy</button>
        <button class="site-footer__btn info-trigger" type="button" data-info="terms">Terms</button>
        <button class="site-footer__btn info-trigger" type="button" data-info="help">Help</button>
      </div>
    </div>
  </footer>

  <div id="infoOverlay" class="info-overlay" aria-hidden="true">
    <div class="info-card" role="dialog" aria-modal="true" aria-labelledby="infoTitle">
      <button class="info-close" type="button" aria-label="Close">x</button>
      <h3 id="infoTitle">Privacy</h3>
      <div id="infoBody" class="info-body"></div>
    </div>
  </div>

  <div id="emailOverlay" class="modal-overlay" aria-hidden="true">
    <div class="modal-card" role="dialog" aria-modal="true" aria-labelledby="emailTitle">
      <button id="closeEmailModal" class="modal-close" type="button" aria-label="Close">x</button>
      <h3 id="emailTitle">Update Email</h3>
      <p>Enter your new email and verification code to confirm.</p>
      <div class="modal-field">
        <label for="newEmailInput">New email</label>
        <input id="newEmailInput" type="email" placeholder="name@example.com" />
      </div>
      <div class="modal-field">
        <label for="emailCodeInput">Verification code</label>
        <div class="code-row">
          <input id="emailCodeInput" type="text" placeholder="Verification code" inputmode="numeric" pattern="[0-9]*" maxlength="6" autocomplete="one-time-code" />
          <button id="sendEmailCodeBtn" class="btn ghost small" type="button">Send code</button>
        </div>
        <div id="emailUpdateErr" class="error-text" style="display:none"></div>
      </div>
      <div class="modal-actions">
        <button id="confirmEmailBtn" class="btn small" type="button">Confirm</button>
        <button id="cancelEmailBtn" class="btn ghost small" type="button">Cancel</button>
      </div>
    </div>
  </div>

  <script>
    document.addEventListener('DOMContentLoaded', () => {
      const infoOverlay = document.getElementById('infoOverlay');
      const infoTitle = document.getElementById('infoTitle');
      const infoBody = document.getElementById('infoBody');
      const infoMap = {
        privacy: {
          title: 'Privacy',
          body: \`
            <p>We only store the information needed to manage bookings and memberships.</p>
            <p>Your account data is kept locally in this demo and is not shared with third parties.</p>
            <p>You can request deletion at any time by contacting the admin.</p>
          \`
        },
        terms: {
          title: 'Terms',
          body: \`
            <p>Bookings are first-come, first-served and subject to club capacity.</p>
            <p>Members must follow club rules and respect facility policies.</p>
            <p>Repeated no-shows may result in booking restrictions.</p>
          \`
        },
        help: {
          title: 'Help',
          body: \`
            <p>Need assistance? Start by searching for a club and selecting a time slot.</p>
            <p>If you cannot log in, double-check your email and password.</p>
            <p>Contact support at support@example.com for further help.</p>
          \`
        }
      };

      const openInfo = (key) => {
        const data = infoMap[key];
        if (!data || !infoOverlay) return;
        if (infoTitle) infoTitle.textContent = data.title;
        if (infoBody) infoBody.innerHTML = data.body;
        infoOverlay.classList.add('open');
        document.body.classList.add('no-scroll');
      };

      const closeInfo = () => {
        if (!infoOverlay) return;
        infoOverlay.classList.remove('open');
        document.body.classList.remove('no-scroll');
      };

      document.querySelectorAll('.info-trigger').forEach((btn) => {
        btn.addEventListener('click', () => openInfo(btn.dataset.info));
      });
      infoOverlay?.addEventListener('click', (evt) => { if (evt.target === infoOverlay) closeInfo(); });
      infoOverlay?.querySelector('.info-close')?.addEventListener('click', closeInfo);

      const panelButtons = Array.from(document.querySelectorAll('.side-link'));
      const panels = Array.from(document.querySelectorAll('.panel'));
      const sideNav = document.querySelector('.side-nav');

      const switchPanel = (panelKey) => {
        panelButtons.forEach((btn) => btn.classList.toggle('active', btn.dataset.panel === panelKey));
        panels.forEach((panel) => panel.classList.toggle('active', panel.dataset.panel === panelKey));
        sideNav?.classList.toggle('info-active', panelKey === 'info');
      };

      panelButtons.forEach((btn) => {
        btn.addEventListener('click', () => switchPanel(btn.dataset.panel));
      });


      const safeParse = (key) => {
        try {
          return JSON.parse(localStorage.getItem(key) || 'null');
        } catch (err) {
          return null;
        }
      };

      // TODO: Replace with real backend endpoints.
      const profileApi = {
        async getProfile() {
          try {
            const res = await fetch('/api/profile', { credentials: 'include' });
            if (res.ok) return await res.json();
          } catch (err) {
            console.error(err);
          }
          return null;
        },
        async updateName(displayName) {
          try {
            const res = await fetch('/api/profile', {
              method: 'PATCH',
              headers: { 'Content-Type': 'application/json' },
              credentials: 'include',
              body: JSON.stringify({ displayName })
            });
            if (res.ok) return await res.json();
          } catch (err) {
            console.error(err);
          }
          return null;
        },
        async sendEmailCode(email) {
          try {
            const res = await fetch('/api/profile/email/code', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              credentials: 'include',
              body: JSON.stringify({ email })
            });
            if (res.ok) return await res.json();
          } catch (err) {
            console.error(err);
          }
          return null;
        },
        async updateEmail(email, code) {
          try {
            const res = await fetch('/api/profile/email', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              credentials: 'include',
              body: JSON.stringify({ email, code })
            });
            if (res.ok) return await res.json();
          } catch (err) {
            console.error(err);
          }
          return null;
        },
        async uploadAvatar(file) {
          try {
            const formData = new FormData();
            formData.append('avatar', file);
            const res = await fetch('/api/profile/avatar', {
              method: 'POST',
              credentials: 'include',
              body: formData
            });
            if (res.ok) return await res.json();
          } catch (err) {
            console.error(err);
          }
          return null;
        }
      };

      const tabProfile = document.getElementById('tabProfile');
      const tabSecurity = document.getElementById('tabSecurity');
      const profileTab = document.getElementById('profileTab');
      const securityTab = document.getElementById('securityTab');

      const modalNameInput = document.getElementById('modalNameInput');
      const nameRow = document.getElementById('nameRow');
      const modalEmailInput = document.getElementById('modalEmailInput');
      const editNameBtn = document.getElementById('editNameBtn');
      const saveNameBtn = document.getElementById('saveNameBtn');
      const cancelNameBtn = document.getElementById('cancelNameBtn');
      const editEmailBtn = document.getElementById('editEmailBtn');
      const emailOverlay = document.getElementById('emailOverlay');
      const closeEmailModal = document.getElementById('closeEmailModal');
      const cancelEmailBtn = document.getElementById('cancelEmailBtn');
      const confirmEmailBtn = document.getElementById('confirmEmailBtn');
      const sendEmailCodeBtn = document.getElementById('sendEmailCodeBtn');
      const newEmailInput = document.getElementById('newEmailInput');
      const emailCodeInput = document.getElementById('emailCodeInput');
      const emailUpdateErr = document.getElementById('emailUpdateErr');
      const nameConfirm = document.getElementById('nameConfirm');
      const confirmNameBtn = document.getElementById('confirmNameBtn');
      const dismissNameBtn = document.getElementById('dismissNameBtn');
      const avatarInput = document.getElementById('avatarInput');
      const avatarImg = document.getElementById('avatarImg');
      const avatarPlaceholder = document.getElementById('avatarPlaceholder');

      const currentPassInput = document.getElementById('currentPassInput');
      const newPassInput = document.getElementById('newPassInput');
      const confirmPassInput = document.getElementById('confirmPassInput');
      const currentPassErr = document.getElementById('currentPassErr');
      const newPassErr = document.getElementById('newPassErr');
      const confirmPassErr = document.getElementById('confirmPassErr');

      let currentName = '';
      let currentEmail = '';
      let codeTimer = null;
      let codeRemaining = 0;

      const toggleConfirm = (show) => {
        if (!nameConfirm) return;
        nameConfirm.classList.toggle('open', show);
        if (show) confirmNameBtn?.focus();
        if (saveNameBtn) saveNameBtn.style.display = show ? 'none' : 'inline-flex';
        if (cancelNameBtn) cancelNameBtn.style.display = show ? 'none' : 'inline-flex';
      };

      const setEditing = (editing) => {
        if (!modalNameInput) return;
        modalNameInput.readOnly = !editing;
        if (editNameBtn) editNameBtn.style.display = editing ? 'none' : 'inline-flex';
        if (saveNameBtn) saveNameBtn.style.display = editing ? 'inline-flex' : 'none';
        if (cancelNameBtn) cancelNameBtn.style.display = editing ? 'inline-flex' : 'none';
        nameRow?.classList.toggle('editing', editing);
        toggleConfirm(false);
        if (editing) modalNameInput.focus();
      };

      const loadAndDisplay = async () => {
        const profile = await profileApi.getProfile();
        const localProfile = safeParse('profile');
        const loggedUser = safeParse('loggedUser');
        const userObj = safeParse('user');
        const avatarData = safeParse('profileAvatar');

        const displayName = (profile && profile.displayName)
          || (localProfile && localProfile.displayName)
          || (loggedUser && loggedUser.name)
          || (userObj && (userObj.fullName || userObj.name))
          || '';
        const email = (profile && profile.email)
          || (loggedUser && loggedUser.email)
          || (userObj && userObj.email)
          || '';
        const avatarUrl = (profile && (profile.avatarUrl || profile.avatar))
          || avatarData
          || '';

        if (modalNameInput) modalNameInput.value = displayName || '';
        if (modalEmailInput) modalEmailInput.value = email || '';
        currentName = displayName || '';
        currentEmail = email || '';
        setEditing(false);

        if (avatarImg && avatarPlaceholder) {
          if (avatarUrl) {
            avatarImg.src = avatarUrl;
            avatarImg.style.display = 'block';
            avatarPlaceholder.style.display = 'none';
          } else {
            avatarImg.style.display = 'none';
            avatarPlaceholder.style.display = 'block';
          }
        }
      };

      const switchInfoTab = (tab) => {
        const showProfile = tab === 'profile';
        profileTab.classList.toggle('active', showProfile);
        securityTab.classList.toggle('active', !showProfile);
        tabProfile.classList.toggle('active', showProfile);
        tabSecurity.classList.toggle('active', !showProfile);
      };

      const resetSecurity = () => {
        currentPassInput.value = '';
        newPassInput.value = '';
        confirmPassInput.value = '';
        currentPassErr.style.display = 'none';
        newPassErr.style.display = 'none';
        confirmPassErr.style.display = 'none';
      };

      tabProfile?.addEventListener('click', () => {
        switchPanel('info');
        switchInfoTab('profile');
      });
      tabSecurity?.addEventListener('click', () => {
        switchPanel('info');
        switchInfoTab('security');
      });

      const persistDisplayName = async () => {
        const newName = (modalNameInput.value || '').trim();
        if (newName === currentName) {
          setEditing(false);
          return;
        }
        const updated = await profileApi.updateName(newName);
        if (!updated) {
          const profile = safeParse('profile') || {};
          const loggedUser = safeParse('loggedUser') || {};
          const userObj = safeParse('user') || {};

          profile.displayName = newName;
          loggedUser.name = newName;
          userObj.fullName = newName;

          try {
            localStorage.setItem('profile', JSON.stringify(profile));
            localStorage.setItem('loggedUser', JSON.stringify(loggedUser));
            localStorage.setItem('user', JSON.stringify(userObj));
          } catch (err) {
            console.error(err);
          }
        }

        currentName = newName;
        setEditing(false);
        toggleConfirm(false);
      };

      const openEmailModal = () => {
        if (!emailOverlay) return;
        if (newEmailInput) newEmailInput.value = '';
        if (emailCodeInput) emailCodeInput.value = '';
        if (emailUpdateErr) {
          emailUpdateErr.textContent = '';
          emailUpdateErr.style.display = 'none';
        }
        emailOverlay.classList.add('open');
      };

      const closeEmailModalFn = () => {
        if (!emailOverlay) return;
        emailOverlay.classList.remove('open');
      };

      editNameBtn?.addEventListener('click', () => setEditing(true));
      saveNameBtn?.addEventListener('click', () => toggleConfirm(true));
      confirmNameBtn?.addEventListener('click', persistDisplayName);
      dismissNameBtn?.addEventListener('click', () => toggleConfirm(false));
      cancelNameBtn?.addEventListener('click', () => {
        if (modalNameInput) modalNameInput.value = currentName;
        setEditing(false);
      });
      editEmailBtn?.addEventListener('click', openEmailModal);
      closeEmailModal?.addEventListener('click', closeEmailModalFn);
      cancelEmailBtn?.addEventListener('click', closeEmailModalFn);
      emailOverlay?.addEventListener('click', (event) => {
        if (event.target === emailOverlay) closeEmailModalFn();
      });
      sendEmailCodeBtn?.addEventListener('click', () => {
        if (!sendEmailCodeBtn || sendEmailCodeBtn.disabled) return;
        const emailValue = (newEmailInput?.value || '').trim();
        const emailOk = /^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$/.test(emailValue);
        if (!emailOk) {
          if (emailUpdateErr) {
            emailUpdateErr.textContent = 'Please enter a valid email before requesting a code.';
            emailUpdateErr.style.display = 'block';
          }
          return;
        }
        if (emailUpdateErr) {
          emailUpdateErr.textContent = '';
          emailUpdateErr.style.display = 'none';
        }
        profileApi.sendEmailCode(emailValue);
        codeRemaining = 120;
        sendEmailCodeBtn.disabled = true;
        sendEmailCodeBtn.textContent = \`Resend in \${codeRemaining}s\`;
        if (codeTimer) clearInterval(codeTimer);
        codeTimer = setInterval(() => {
          codeRemaining -= 1;
          if (codeRemaining <= 0) {
            clearInterval(codeTimer);
            codeTimer = null;
            sendEmailCodeBtn.disabled = false;
            sendEmailCodeBtn.textContent = 'Send code';
            return;
          }
          sendEmailCodeBtn.textContent = \`Resend in \${codeRemaining}s\`;
        }, 1000);
      });
      confirmEmailBtn?.addEventListener('click', async () => {
        const nextEmail = (newEmailInput?.value || '').trim();
        const code = (emailCodeInput?.value || '').trim();
        if (!nextEmail || !code) {
          if (emailUpdateErr) {
            emailUpdateErr.textContent = 'Please enter the new email and verification code.';
            emailUpdateErr.style.display = 'block';
          }
          return;
        }
        if (!/^\\d{6}$/.test(code)) {
          if (emailUpdateErr) {
            emailUpdateErr.textContent = 'Verification code must be 6 digits.';
            emailUpdateErr.style.display = 'block';
          }
          return;
        }
        if (emailUpdateErr) {
          emailUpdateErr.textContent = '';
          emailUpdateErr.style.display = 'none';
        }
        const updated = await profileApi.updateEmail(nextEmail, code);
        if (!updated) {
          if (emailUpdateErr) {
            emailUpdateErr.textContent = 'Verification failed. Please check the code and try again.';
            emailUpdateErr.style.display = 'block';
          }
          return;
        }
        currentEmail = nextEmail;
        if (modalEmailInput) modalEmailInput.value = nextEmail;
        closeEmailModalFn();
      });

      emailCodeInput?.addEventListener('input', () => {
        if (!emailCodeInput) return;
        const cleaned = emailCodeInput.value.replace(/\\D/g, '').slice(0, 6);
        if (emailCodeInput.value !== cleaned) emailCodeInput.value = cleaned;
      });
      modalNameInput?.addEventListener('keydown', (event) => {
        if (modalNameInput.readOnly) {
          if (event.key === 'Enter' || event.key === ' ') {
            event.preventDefault();
            setEditing(true);
            return;
          }
          if (event.key.length === 1 || event.key === 'Backspace' || event.key === 'Delete') {
            setEditing(true);
          }
        }
        if (event.key === 'Enter') {
          event.preventDefault();
          toggleConfirm(true);
        }
      });

      avatarInput?.addEventListener('change', async (event) => {
        const file = event.target.files && event.target.files[0];
        if (!file) return;
        const reader = new FileReader();
        reader.onload = () => {
          const result = reader.result;
          if (typeof result !== 'string') return;
          try {
            localStorage.setItem('profileAvatar', JSON.stringify(result));
          } catch (err) {
            console.error(err);
          }
          if (avatarImg && avatarPlaceholder) {
            avatarImg.src = result;
            avatarImg.style.display = 'block';
            avatarPlaceholder.style.display = 'none';
          }
        };
        reader.readAsDataURL(file);
        await profileApi.uploadAvatar(file);
      });

      document.getElementById('updatePassBtn')?.addEventListener('click', () => {
        currentPassErr.style.display = 'none';
        newPassErr.style.display = 'none';
        confirmPassErr.style.display = 'none';

        const currentPass = currentPassInput.value;
        const newPass = newPassInput.value;
        const confirmPass = confirmPassInput.value;

        let valid = true;

        if (!currentPass) {
          currentPassErr.textContent = 'Current password is required.';
          currentPassErr.style.display = 'block';
          valid = false;
        }
        if (!newPass) {
          newPassErr.textContent = 'New password is required.';
          newPassErr.style.display = 'block';
          valid = false;
        }
        if (newPass && confirmPass && newPass !== confirmPass) {
          confirmPassErr.textContent = 'Passwords do not match.';
          confirmPassErr.style.display = 'block';
          valid = false;
        }

        if (!valid) return;

        const updateBtn = document.getElementById('updatePassBtn');
        if (updateBtn) {
          updateBtn.textContent = 'Password updated';
          setTimeout(() => {
            updateBtn.textContent = 'Update Password';
            resetSecurity();
          }, 900);
        }
      });

      loadAndDisplay();
      switchPanel('bookings');
    });
  <\/script>

  <script src="auth-modal.js" defer><\/script>
</body>
</html>
`,Hc=`<!doctype html>\r
<html lang="en">\r
<head>\r
  <meta charset="utf-8">\r
  <meta name="viewport" content="width=device-width,initial-scale=1">\r
  <title>Venue overview</title>\r
  <link rel="stylesheet" href="theme.css">\r
  <style>\r
    body {\r
      margin: 0;\r
      background: #f7f8fb;\r
      color: #0f172a;\r
      font-family: "Space Grotesk", "Segoe UI", sans-serif;\r
    }\r
    .wrap {\r
      max-width: 980px;\r
      margin: 40px auto 80px;\r
      padding: 24px;\r
    }\r
    .hero {\r
      background: #ffffff;\r
      border-radius: 22px;\r
      padding: 24px;\r
      border: 1px solid rgba(15, 23, 42, 0.08);\r
      box-shadow: 0 18px 40px rgba(15, 23, 42, 0.08);\r
      display: grid;\r
      gap: 8px;\r
      position: relative;\r
    }\r
    .hero h1 {\r
      margin: 0;\r
      font-size: 28px;\r
    }\r
    .hero p {\r
      margin: 0;\r
      color: #5b6476;\r
    }\r
    .back-btn {\r
      position: absolute;\r
      top: 18px;\r
      right: 18px;\r
      border: 1px solid rgba(15, 23, 42, 0.12);\r
      background: #ffffff;\r
      color: #111111;\r
      border-radius: 999px;\r
      padding: 8px 14px;\r
      font-size: 12px;\r
      font-weight: 600;\r
      cursor: pointer;\r
      text-decoration: none;\r
    }\r
    .panel {\r
      margin-top: 24px;\r
      background: #ffffff;\r
      border-radius: 22px;\r
      padding: 24px;\r
      border: 1px solid rgba(15, 23, 42, 0.08);\r
      box-shadow: 0 18px 40px rgba(15, 23, 42, 0.08);\r
      display: grid;\r
      gap: 16px;\r
    }\r
    .panel-header {\r
      display: flex;\r
      align-items: flex-start;\r
      justify-content: space-between;\r
      gap: 12px;\r
      flex-wrap: wrap;\r
    }\r
    .panel-header h2 {\r
      margin: 0 0 6px;\r
      font-size: 20px;\r
    }\r
    .panel-header p {\r
      margin: 0;\r
      color: #6b7280;\r
      font-size: 13px;\r
    }\r
    .filter-toggle {\r
      border: 1px solid rgba(148, 163, 184, 0.35);\r
      background: linear-gradient(180deg, #ffffff, #f5f7ff);\r
      color: #15171a;\r
      border-radius: 999px;\r
      padding: 8px 14px;\r
      font-size: 12px;\r
      font-weight: 600;\r
      cursor: pointer;\r
      transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;\r
    }\r
    .filter-toggle:hover {\r
      transform: translateY(-1px);\r
      box-shadow: 0 12px 30px rgba(15, 23, 42, 0.12);\r
    }\r
    .filter-toggle.active {\r
      background: #111111;\r
      color: #ffffff;\r
      border-color: #111111;\r
    }\r
    .filter-count {\r
      display: inline-flex;\r
      align-items: center;\r
      justify-content: center;\r
      min-width: 18px;\r
      height: 18px;\r
      margin-left: 6px;\r
      border-radius: 999px;\r
      background: rgba(17, 17, 17, 0.1);\r
      font-size: 11px;\r
      padding: 0 6px;\r
    }\r
    .filter-surface {\r
      display: none;\r
      margin-top: 12px;\r
      border: 1px solid var(--line);\r
      border-radius: 16px;\r
      padding: 12px;\r
      background: #ffffff;\r
      box-shadow: 0 12px 30px rgba(15, 23, 42, 0.08);\r
    }\r
    .filter-surface.open {\r
      display: block;\r
    }\r
    .filter-surface__body {\r
      display: none;\r
      gap: 10px;\r
      margin-top: 0;\r
    }\r
    .filter-surface.open .filter-surface__body {\r
      display: grid;\r
    }\r
    .filter-panel {\r
      display: flex;\r
      gap: 8px;\r
      flex-wrap: wrap;\r
      width: 100%;\r
    }\r
    .filter-chip {\r
      border: 1px solid var(--line);\r
      background: var(--chip);\r
      color: #15171a;\r
      border-radius: 999px;\r
      padding: 6px 12px;\r
      font-size: 12px;\r
      font-weight: 600;\r
      cursor: pointer;\r
      transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;\r
    }\r
    .filter-chip:hover {\r
      transform: translateY(-1px);\r
      background: #ffffff;\r
      box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);\r
    }\r
    .filter-chip.active {\r
      background: #111111;\r
      color: #ffffff;\r
      border-color: #111111;\r
    }\r
    .summary-list {\r
      display: grid;\r
      gap: 18px;\r
    }\r
    .summary-group {\r
      display: grid;\r
      gap: 10px;\r
    }\r
    .summary-title {\r
      margin: 0;\r
      font-size: 14px;\r
      font-weight: 600;\r
      color: #0f172a;\r
    }\r
    .panel-empty {\r
      color: #6b7280;\r
      font-size: 13px;\r
      border: 1px dashed rgba(148, 163, 184, 0.5);\r
      border-radius: 14px;\r
      padding: 16px;\r
      background: rgba(248, 250, 252, 0.9);\r
    }\r
    .court-list {\r
      display: grid;\r
      gap: 12px;\r
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));\r
    }\r
    .court-card {\r
      position: relative;\r
      border-radius: 16px;\r
      padding: 14px;\r
      border: 1px solid rgba(15, 23, 42, 0.08);\r
      background: #ffffff;\r
      display: grid;\r
      gap: 10px;\r
      cursor: pointer;\r
      transition: transform 0.2s ease, box-shadow 0.2s ease;\r
    }\r
    .court-card:hover {\r
      transform: translateY(-1px);\r
      box-shadow: 0 12px 24px rgba(15, 23, 42, 0.08);\r
    }\r
    .court-card:focus-visible {\r
      outline: 2px solid rgba(37, 99, 235, 0.4);\r
      outline-offset: 2px;\r
    }\r
    .court-card__row {\r
      display: grid;\r
      grid-template-columns: auto minmax(0, 1fr) auto;\r
      align-items: center;\r
      gap: 10px;\r
    }\r
    .court-meta {\r
      font-size: 12px;\r
      font-weight: 600;\r
      color: #6b7280;\r
      grid-column: 2;\r
    }\r
    .court-badge {\r
      width: 36px;\r
      height: 36px;\r
      border-radius: 12px;\r
      display: inline-flex;\r
      align-items: center;\r
      justify-content: center;\r
      background: rgba(37, 99, 235, 0.12);\r
      color: #1d4ed8;\r
      font-weight: 600;\r
      border: 1px solid rgba(37, 99, 235, 0.2);\r
    }\r
    .court-name {\r
      width: 100%;\r
      height: 36px;\r
      border-radius: 10px;\r
      border: 1px solid rgba(15, 23, 42, 0.12);\r
      padding: 0 10px;\r
      font-size: 13px;\r
      flex: 1 1 160px;\r
      max-width: 200px;\r
      min-width: 120px;\r
      justify-self: start;\r
    }\r
    .court-confirm {\r
      border: none;\r
      border-radius: 999px;\r
      padding: 6px 12px;\r
      font-size: 12px;\r
      font-weight: 600;\r
      cursor: pointer;\r
      background: #111111;\r
      color: #ffffff;\r
      white-space: nowrap;\r
      grid-column: 3;\r
      justify-self: start;\r
    }\r
    .court-confirm:hover {\r
      background: #000000;\r
    }\r
    .court-manage {\r
      border: 1px solid rgba(15, 23, 42, 0.12);\r
      background: #111111;\r
      color: #ffffff;\r
      border-radius: 999px;\r
      padding: 6px 12px;\r
      font-size: 12px;\r
      font-weight: 600;\r
      cursor: pointer;\r
      grid-column: 3;\r
      justify-self: start;\r
    }\r
    .court-manage:hover {\r
      background: #000000;\r
    }\r
    .slot-list {\r
      display: grid;\r
      gap: 12px;\r
      grid-template-columns: repeat(2, minmax(0, 1fr));\r
    }\r
    @media (max-width: 720px) {\r
      .slot-list {\r
        grid-template-columns: 1fr;\r
      }\r
    }\r
    .slot-card {\r
      position: relative;\r
      border-radius: 16px;\r
      padding: 14px;\r
      border: 1px solid rgba(15, 23, 42, 0.08);\r
      background: #ffffff;\r
      display: grid;\r
      gap: 8px;\r
    }\r
    .slot-badge {\r
      width: 36px;\r
      height: 36px;\r
      border-radius: 12px;\r
      display: inline-flex;\r
      align-items: center;\r
      justify-content: center;\r
      background: rgba(37, 99, 235, 0.12);\r
      color: #1d4ed8;\r
      font-weight: 600;\r
      border: 1px solid rgba(37, 99, 235, 0.2);\r
    }\r
    .slot-label {\r
      font-size: 12px;\r
      font-weight: 600;\r
      color: #0f172a;\r
    }\r
    .slot-row {\r
      display: flex;\r
      align-items: center;\r
      gap: 8px;\r
    }\r
    .slot-input {\r
      width: 100%;\r
      height: 36px;\r
      border-radius: 10px;\r
      border: 1px solid rgba(15, 23, 42, 0.12);\r
      padding: 0 10px;\r
      font-size: 13px;\r
    }\r
    .slot-input[type="time"] {\r
      cursor: pointer;\r
    }\r
    .slot-sep {\r
      font-size: 12px;\r
      color: #6b7280;\r
      white-space: nowrap;\r
    }\r
    .slot-actions {\r
      display: flex;\r
      align-items: center;\r
      justify-content: space-between;\r
      gap: 10px;\r
      margin-top: 2px;\r
    }\r
    .slot-status {\r
      font-size: 11px;\r
      font-weight: 600;\r
      color: #6b7280;\r
      padding: 4px 8px;\r
      border-radius: 999px;\r
      background: rgba(15, 23, 42, 0.06);\r
    }\r
    .slot-confirm {\r
      border: none;\r
      border-radius: 999px;\r
      padding: 6px 12px;\r
      font-size: 12px;\r
      font-weight: 600;\r
      cursor: pointer;\r
      background: #111111;\r
      color: #ffffff;\r
    }\r
    .slot-confirm:hover {\r
      background: #000000;\r
    }\r
    .summary-card input:disabled {\r
      background: rgba(248, 250, 252, 0.9);\r
      color: #0f172a;\r
      opacity: 1;\r
    }\r
    .slot-card.is-confirmed {\r
      border-color: rgba(34, 197, 94, 0.3);\r
      box-shadow: 0 12px 24px rgba(34, 197, 94, 0.12);\r
    }\r
    .slot-card.is-confirmed .slot-status {\r
      background: rgba(34, 197, 94, 0.12);\r
      color: #15803d;\r
    }\r
    .add-slot {\r
      justify-self: start;\r
      padding: 8px 14px;\r
      border-radius: 999px;\r
      font-size: 12px;\r
      border: 1px solid rgba(15, 23, 42, 0.12);\r
      background: #ffffff;\r
      cursor: pointer;\r
    }\r
    .slot-modal {\r
      position: fixed;\r
      inset: 0;\r
      display: none;\r
      align-items: center;\r
      justify-content: center;\r
      padding: 24px;\r
      background: rgba(10, 12, 16, 0.45);\r
      z-index: 4000;\r
    }\r
    .slot-modal.open {\r
      display: flex;\r
    }\r
    .slot-modal__card {\r
      width: min(860px, 92vw);\r
      max-height: 86vh;\r
      overflow: auto;\r
      background: #ffffff;\r
      border-radius: 22px;\r
      padding: 24px;\r
      border: 1px solid rgba(15, 23, 42, 0.08);\r
      box-shadow: 0 30px 70px rgba(15, 23, 42, 0.25);\r
      position: relative;\r
      display: grid;\r
      gap: 16px;\r
    }\r
    .slot-modal__close {\r
      position: absolute;\r
      top: 16px;\r
      right: 16px;\r
      width: 32px;\r
      height: 32px;\r
      border-radius: 999px;\r
      border: 1px solid rgba(15, 23, 42, 0.12);\r
      background: #ffffff;\r
      cursor: pointer;\r
      font-size: 16px;\r
    }\r
    .slot-modal__header {\r
      display: flex;\r
      align-items: flex-start;\r
      justify-content: space-between;\r
      gap: 12px;\r
      flex-wrap: wrap;\r
      padding-right: 48px;\r
    }\r
  </style>\r
</head>\r
<body>\r
  <header style="position: sticky; top: 0; z-index: 10; border-bottom: 1px solid #e5e7eb; background: white; padding: 12px 0;">\r
    <div class="wrap" style="max-width: 1100px; margin: 0 auto; padding: 0 16px; display: flex; align-items: center; gap: 16px;">\r
      <button onclick="window.parent.location.href = '/club-home'" type="button" style="border: none; background: none; cursor: pointer; font-size: 14px; color: #0066cc; text-decoration: none; padding: 8px 0;">← Back</button>\r
      <h2 style="margin: 0; font-size: 18px; font-weight: 600;">Venue overview</h2>\r
    </div>\r
  </header>\r
  <main class="wrap">\r
    <section class="hero" style="margin-top: 24px;">\r
      <h1>Venue overview</h1>\r
      <p>Adjust time and positions per slot (placeholder only).</p>\r
    </section>\r
\r
    <section class="panel">\r
      <div class="panel-header">\r
        <div>\r
          <h2>Manage courts</h2>\r
          <p>Create courts and open each one to edit booking slots.</p>\r
        </div>\r
        <button id="addCourt" class="add-slot" type="button">Add court</button>\r
      </div>\r
      <div id="courtList" class="court-list"></div>\r
    </section>\r
\r
    <section class="panel">\r
      <div class="panel-header">\r
        <div>\r
          <h2>Booking slots</h2>\r
          <p>Select a court above to manage its slots.</p>\r
        </div>\r
        <button class="filter-toggle" type="button" id="slotFilterToggle" aria-expanded="false" aria-controls="slotFilterSurface">\r
          Filter <span class="filter-count" id="slotFilterCount" style="display:none">0</span>\r
        </button>\r
      </div>\r
      <div class="filter-surface" id="slotFilterSurface">\r
        <div class="filter-surface__body">\r
          <div class="filter-panel" id="slotFilterPanel" aria-label="Filter slots by court"></div>\r
        </div>\r
      </div>\r
      <div id="bookingSummary" class="summary-list"></div>\r
    </section>\r
  </main>\r
\r
  <div id="slotModal" class="slot-modal" aria-hidden="true">\r
    <div class="slot-modal__card" role="dialog" aria-modal="true" aria-labelledby="modalTitle">\r
      <button id="modalClose" class="slot-modal__close" type="button" aria-label="Close">x</button>\r
      <div class="slot-modal__header">\r
        <div>\r
          <h2 id="modalTitle">Booking slots</h2>\r
          <p>Adjust time and positions for this court (placeholder).</p>\r
        </div>\r
        <button id="modalAddSlot" class="add-slot" type="button">Add slot</button>\r
      </div>\r
      <div id="modalSlotList" class="slot-list"></div>\r
    </div>\r
  </div>\r
\r
  <script>\r
    const courtList = document.getElementById('courtList');\r
    const addCourtBtn = document.getElementById('addCourt');\r
    const slotModal = document.getElementById('slotModal');\r
    const modalTitle = document.getElementById('modalTitle');\r
    const modalSlotList = document.getElementById('modalSlotList');\r
    const modalAddSlot = document.getElementById('modalAddSlot');\r
    const modalClose = document.getElementById('modalClose');\r
    const bookingSummary = document.getElementById('bookingSummary');\r
    const slotFilterToggle = document.getElementById('slotFilterToggle');\r
    const slotFilterSurface = document.getElementById('slotFilterSurface');\r
    const slotFilterPanel = document.getElementById('slotFilterPanel');\r
    const slotFilterCount = document.getElementById('slotFilterCount');\r
\r
    // Placeholder API layer (swap endpoints when backend is ready).\r
    const DB_API_BASE = '';\r
    const COURTS_ENDPOINT = '/api/club/courts';\r
    const SLOTS_ENDPOINT = '/api/club/slots';\r
\r
    const apiRequest = async (path, options = {}) => {\r
      if (!DB_API_BASE) return null;\r
      try {\r
        const response = await fetch(\`\${DB_API_BASE}\${path}\`, {\r
          headers: { 'Content-Type': 'application/json' },\r
          ...options,\r
        });\r
        if (!response.ok) throw new Error(\`API \${response.status}\`);\r
        return await response.json();\r
      } catch (error) {\r
        console.warn('[venue overview] API error', error);\r
        return null;\r
      }\r
    };\r
\r
    const fetchCourtsFromApi = async () => apiRequest(COURTS_ENDPOINT);\r
    const saveCourtToApi = async (court) => apiRequest(COURTS_ENDPOINT, {\r
      method: 'POST',\r
      body: JSON.stringify(court),\r
    });\r
    const saveSlotToApi = async (courtId, slot) => apiRequest(SLOTS_ENDPOINT, {\r
      method: 'POST',\r
      body: JSON.stringify({ courtId, ...slot }),\r
    });\r
\r
    let courts = [];\r
    let courtCount = 0;\r
    let activeCourtId = null;\r
    const selectedCourtFilters = [];\r
\r
    const courtKey = (court) => \`court-\${court.id}\`;\r
\r
    const createSlot = (start = '08:00', end = '09:00', positions = 1) => ({\r
      id: null,\r
      start,\r
      end,\r
      positions,\r
      confirmed: false,\r
    });\r
\r
    const createCourt = () => {\r
      courtCount += 1;\r
      return {\r
        id: courtCount,\r
        name: \`Court \${courtCount}\`,\r
        nameConfirmed: false,\r
        slots: [createSlot('08:00', '09:00', 1), createSlot('09:00', '10:00', 1)],\r
      };\r
    };\r
\r
    const normalizeCourts = (data) => (\r
      data.map((court, index) => ({\r
        id: Number(court.id) || index + 1,\r
        name: court.name || \`Court \${index + 1}\`,\r
        nameConfirmed: Boolean(court.nameConfirmed),\r
        slots: Array.isArray(court.slots)\r
          ? court.slots.map((slot, slotIndex) => ({\r
            id: slot.id ?? null,\r
            start: slot.start || '08:00',\r
            end: slot.end || '09:00',\r
            positions: Number(slot.positions) || 1,\r
            confirmed: Boolean(slot.confirmed),\r
          }))\r
          : [],\r
      }))\r
    );\r
\r
    const getActiveCourt = () => courts.find((court) => court.id === activeCourtId);\r
\r
    const openModal = (courtId) => {\r
      const court = courts.find((item) => item.id === courtId);\r
      if (!court || !slotModal) return;\r
      activeCourtId = courtId;\r
      const name = court.name && court.name.trim() ? court.name.trim() : \`Court \${court.id}\`;\r
      if (modalTitle) {\r
        modalTitle.textContent = \`Booking slots - \${name}\`;\r
      }\r
      slotModal.classList.add('open');\r
      slotModal.setAttribute('aria-hidden', 'false');\r
      renderSlots();\r
    };\r
\r
    const closeModal = () => {\r
      if (!slotModal) return;\r
      slotModal.classList.remove('open');\r
      slotModal.setAttribute('aria-hidden', 'true');\r
      activeCourtId = null;\r
    };\r
\r
    const renderCourts = () => {\r
      if (!courtList) return;\r
      courtList.innerHTML = '';\r
\r
      if (!courts.length) {\r
        const empty = document.createElement('div');\r
        empty.className = 'panel-empty';\r
        empty.textContent = 'Add a court to start managing booking slots.';\r
        courtList.appendChild(empty);\r
        return;\r
      }\r
\r
      courts.forEach((court, index) => {\r
        const card = document.createElement('div');\r
        card.className = 'court-card';\r
        card.tabIndex = 0;\r
        card.setAttribute('role', 'button');\r
\r
        const rowTop = document.createElement('div');\r
        rowTop.className = 'court-card__row';\r
\r
        const badge = document.createElement('div');\r
        badge.className = 'court-badge';\r
        badge.textContent = String(index + 1);\r
\r
        const nameInput = document.createElement('input');\r
        nameInput.type = 'text';\r
        nameInput.className = 'court-name';\r
        nameInput.value = court.name;\r
        nameInput.placeholder = 'Court name';\r
        nameInput.addEventListener('input', (event) => {\r
          court.name = event.target.value;\r
          if (activeCourtId === court.id && modalTitle) {\r
            const displayName = court.name && court.name.trim() ? court.name.trim() : \`Court \${court.id}\`;\r
            modalTitle.textContent = \`Booking slots - \${displayName}\`;\r
          }\r
          renderSlotFilterChips();\r
          renderSummary();\r
        });\r
        nameInput.addEventListener('click', (event) => event.stopPropagation());\r
        nameInput.addEventListener('keydown', (event) => event.stopPropagation());\r
\r
        const confirmBtn = document.createElement('button');\r
        confirmBtn.type = 'button';\r
        confirmBtn.className = 'court-confirm';\r
\r
        const setNameConfirmed = (confirmed) => {\r
          court.nameConfirmed = confirmed;\r
          nameInput.disabled = confirmed;\r
          confirmBtn.textContent = confirmed ? 'Edit' : 'Confirm';\r
          renderSlotFilterChips();\r
          renderSummary();\r
          if (confirmed) {\r
            void saveCourtToApi(court);\r
          }\r
        };\r
\r
        confirmBtn.addEventListener('click', (event) => {\r
          event.stopPropagation();\r
          setNameConfirmed(!court.nameConfirmed);\r
        });\r
\r
        rowTop.appendChild(badge);\r
        rowTop.appendChild(nameInput);\r
        rowTop.appendChild(confirmBtn);\r
\r
        const rowBottom = document.createElement('div');\r
        rowBottom.className = 'court-card__row';\r
\r
        const meta = document.createElement('div');\r
        meta.className = 'court-meta';\r
        const slotCount = court.slots.length;\r
        meta.textContent = \`\${slotCount} slot\${slotCount === 1 ? '' : 's'}\`;\r
\r
        const manageBtn = document.createElement('button');\r
        manageBtn.type = 'button';\r
        manageBtn.className = 'court-manage';\r
        manageBtn.textContent = 'Manage slots';\r
        manageBtn.addEventListener('click', (event) => {\r
          event.stopPropagation();\r
          openModal(court.id);\r
        });\r
\r
        rowBottom.appendChild(meta);\r
        rowBottom.appendChild(manageBtn);\r
\r
        card.appendChild(rowTop);\r
        card.appendChild(rowBottom);\r
\r
        card.addEventListener('click', () => openModal(court.id));\r
        card.addEventListener('keydown', (event) => {\r
          if (event.key === 'Enter' || event.key === ' ') {\r
            event.preventDefault();\r
            openModal(court.id);\r
          }\r
        });\r
\r
        courtList.appendChild(card);\r
\r
        setNameConfirmed(Boolean(court.nameConfirmed));\r
      });\r
    };\r
\r
    const updateSlotFilterUI = () => {\r
      if (slotFilterPanel) {\r
        slotFilterPanel.querySelectorAll('.filter-chip').forEach((btn) => {\r
          btn.classList.toggle('active', selectedCourtFilters.includes(btn.dataset.key));\r
        });\r
      }\r
      if (slotFilterToggle) {\r
        slotFilterToggle.classList.toggle('active', selectedCourtFilters.length > 0);\r
      }\r
      if (slotFilterCount) {\r
        if (selectedCourtFilters.length) {\r
          slotFilterCount.style.display = 'inline-flex';\r
          slotFilterCount.textContent = String(selectedCourtFilters.length);\r
        } else {\r
          slotFilterCount.style.display = 'none';\r
        }\r
      }\r
    };\r
\r
    const renderSlotFilterChips = () => {\r
      if (!slotFilterPanel) return;\r
      slotFilterPanel.innerHTML = '';\r
\r
      const chips = courts.map((court) => ({\r
        key: courtKey(court),\r
        label: court.name && court.name.trim() ? court.name.trim() : \`Court \${court.id}\`,\r
      }));\r
\r
      slotFilterPanel.innerHTML = chips.map((chip) => (\r
        \`<button class="filter-chip" type="button" data-key="\${chip.key}"># \${chip.label}</button>\`\r
      )).join('');\r
\r
      slotFilterPanel.querySelectorAll('.filter-chip').forEach((btn) => {\r
        btn.addEventListener('click', () => {\r
          const key = btn.dataset.key;\r
          const idx = selectedCourtFilters.indexOf(key);\r
          if (idx > -1) {\r
            selectedCourtFilters.splice(idx, 1);\r
          } else {\r
            selectedCourtFilters.push(key);\r
          }\r
          updateSlotFilterUI();\r
          renderSummary();\r
        });\r
      });\r
\r
      updateSlotFilterUI();\r
    };\r
\r
    const renderSummary = () => {\r
      if (!bookingSummary) return;\r
      bookingSummary.innerHTML = '';\r
\r
      const filteredCourts = selectedCourtFilters.length\r
        ? courts.filter((court) => selectedCourtFilters.includes(courtKey(court)))\r
        : courts;\r
      const hasConfirmedSlots = filteredCourts.some((court) => court.slots.some((slot) => slot.confirmed));\r
      if (!hasConfirmedSlots) {\r
        const empty = document.createElement('div');\r
        empty.className = 'panel-empty';\r
        empty.textContent = selectedCourtFilters.length\r
          ? 'No confirmed slots match the selected courts.'\r
          : 'Confirm a slot to show it here.';\r
        bookingSummary.appendChild(empty);\r
        return;\r
      }\r
\r
      filteredCourts.forEach((court) => {\r
        const confirmedSlots = court.slots.filter((slot) => slot.confirmed);\r
        if (!confirmedSlots.length) return;\r
\r
        const group = document.createElement('div');\r
        group.className = 'summary-group';\r
\r
        const title = document.createElement('div');\r
        title.className = 'summary-title';\r
        const name = court.name && court.name.trim() ? court.name.trim() : \`Court \${court.id}\`;\r
        title.textContent = \`Court: \${name}\`;\r
\r
        const list = document.createElement('div');\r
        list.className = 'slot-list';\r
\r
        confirmedSlots.forEach((slot, index) => {\r
          const card = document.createElement('div');\r
          card.className = 'slot-card summary-card';\r
          card.classList.toggle('is-confirmed', Boolean(slot.confirmed));\r
\r
          const badge = document.createElement('div');\r
          badge.className = 'slot-badge';\r
          badge.textContent = String(index + 1);\r
\r
          const timeLabel = document.createElement('div');\r
          timeLabel.className = 'slot-label';\r
          timeLabel.textContent = 'Time';\r
\r
          const timeRow = document.createElement('div');\r
          timeRow.className = 'slot-row';\r
\r
          const startTime = document.createElement('input');\r
          startTime.type = 'time';\r
          startTime.className = 'slot-input';\r
          startTime.value = slot.start;\r
          startTime.disabled = true;\r
\r
          const sep = document.createElement('span');\r
          sep.className = 'slot-sep';\r
          sep.textContent = 'to';\r
\r
          const endTime = document.createElement('input');\r
          endTime.type = 'time';\r
          endTime.className = 'slot-input';\r
          endTime.value = slot.end;\r
          endTime.disabled = true;\r
\r
          timeRow.appendChild(startTime);\r
          timeRow.appendChild(sep);\r
          timeRow.appendChild(endTime);\r
\r
          const spotsLabel = document.createElement('div');\r
          spotsLabel.className = 'slot-label';\r
          spotsLabel.textContent = 'Positions';\r
\r
          const spotsInput = document.createElement('input');\r
          spotsInput.type = 'number';\r
          spotsInput.className = 'slot-input';\r
          spotsInput.value = slot.positions;\r
          spotsInput.disabled = true;\r
\r
          const actions = document.createElement('div');\r
          actions.className = 'slot-actions';\r
\r
          const status = document.createElement('div');\r
          status.className = 'slot-status';\r
          status.textContent = slot.confirmed ? 'Confirmed' : 'Needs confirmation';\r
\r
          actions.appendChild(status);\r
\r
          card.appendChild(badge);\r
          card.appendChild(timeLabel);\r
          card.appendChild(timeRow);\r
          card.appendChild(spotsLabel);\r
          card.appendChild(spotsInput);\r
          card.appendChild(actions);\r
\r
          list.appendChild(card);\r
        });\r
\r
        group.appendChild(title);\r
        group.appendChild(list);\r
        bookingSummary.appendChild(group);\r
      });\r
    };\r
\r
    const renderSlots = () => {\r
      if (!modalSlotList) return;\r
      modalSlotList.innerHTML = '';\r
\r
      const court = getActiveCourt();\r
      if (!court) return;\r
\r
      if (!court.slots.length) {\r
        const empty = document.createElement('div');\r
        empty.className = 'panel-empty';\r
        empty.textContent = 'No slots yet. Add a slot to get started.';\r
        modalSlotList.appendChild(empty);\r
        return;\r
      }\r
\r
      court.slots.forEach((slot, index) => {\r
        const card = document.createElement('div');\r
        card.className = 'slot-card';\r
\r
        const badge = document.createElement('div');\r
        badge.className = 'slot-badge';\r
        badge.textContent = String(index + 1);\r
\r
        const timeLabel = document.createElement('div');\r
        timeLabel.className = 'slot-label';\r
        timeLabel.textContent = 'Time';\r
\r
        const timeRow = document.createElement('div');\r
        timeRow.className = 'slot-row';\r
\r
        const startTime = document.createElement('input');\r
        startTime.type = 'time';\r
        startTime.className = 'slot-input';\r
        startTime.value = slot.start;\r
        startTime.addEventListener('click', () => {\r
          if (typeof startTime.showPicker === 'function') {\r
            startTime.showPicker();\r
          }\r
        });\r
        startTime.addEventListener('input', (event) => {\r
          slot.start = event.target.value;\r
          renderSummary();\r
        });\r
\r
        const sep = document.createElement('span');\r
        sep.className = 'slot-sep';\r
        sep.textContent = 'to';\r
\r
        const endTime = document.createElement('input');\r
        endTime.type = 'time';\r
        endTime.className = 'slot-input';\r
        endTime.value = slot.end;\r
        endTime.addEventListener('click', () => {\r
          if (typeof endTime.showPicker === 'function') {\r
            endTime.showPicker();\r
          }\r
        });\r
        endTime.addEventListener('input', (event) => {\r
          slot.end = event.target.value;\r
          renderSummary();\r
        });\r
\r
        timeRow.appendChild(startTime);\r
        timeRow.appendChild(sep);\r
        timeRow.appendChild(endTime);\r
\r
        const spotsLabel = document.createElement('div');\r
        spotsLabel.className = 'slot-label';\r
        spotsLabel.textContent = 'Positions';\r
\r
        const spotsInput = document.createElement('input');\r
        spotsInput.type = 'number';\r
        spotsInput.min = '1';\r
        spotsInput.className = 'slot-input';\r
        spotsInput.value = slot.positions;\r
        spotsInput.addEventListener('input', (event) => {\r
          slot.positions = event.target.value;\r
          renderSummary();\r
        });\r
\r
        const actions = document.createElement('div');\r
        actions.className = 'slot-actions';\r
\r
        const status = document.createElement('div');\r
        status.className = 'slot-status';\r
\r
        const confirmBtn = document.createElement('button');\r
        confirmBtn.type = 'button';\r
        confirmBtn.className = 'slot-confirm';\r
\r
        const setConfirmed = (confirmed) => {\r
          slot.confirmed = confirmed;\r
          card.classList.toggle('is-confirmed', confirmed);\r
          confirmBtn.textContent = confirmed ? 'Edit' : 'Confirm';\r
          status.textContent = confirmed ? 'Confirmed' : 'Needs confirmation';\r
          [startTime, endTime, spotsInput].forEach((input) => {\r
            input.disabled = confirmed;\r
          });\r
          renderSummary();\r
          if (confirmed) {\r
            void saveSlotToApi(court.id, slot);\r
          }\r
        };\r
\r
        confirmBtn.addEventListener('click', () => {\r
          setConfirmed(!slot.confirmed);\r
        });\r
\r
        actions.appendChild(status);\r
        actions.appendChild(confirmBtn);\r
\r
        card.appendChild(badge);\r
        card.appendChild(timeLabel);\r
        card.appendChild(timeRow);\r
        card.appendChild(spotsLabel);\r
        card.appendChild(spotsInput);\r
        card.appendChild(actions);\r
\r
        modalSlotList.appendChild(card);\r
\r
        setConfirmed(Boolean(slot.confirmed));\r
      });\r
    };\r
\r
    addCourtBtn?.addEventListener('click', () => {\r
      const newCourt = createCourt();\r
      courts.push(newCourt);\r
      renderCourts();\r
      renderSlotFilterChips();\r
      renderSummary();\r
      openModal(newCourt.id);\r
      void saveCourtToApi(newCourt);\r
    });\r
\r
    modalAddSlot?.addEventListener('click', () => {\r
      const court = getActiveCourt();\r
      if (!court) return;\r
      const slot = createSlot();\r
      court.slots.push(slot);\r
      renderSlots();\r
      renderCourts();\r
      renderSlotFilterChips();\r
      renderSummary();\r
      void saveSlotToApi(court.id, slot);\r
    });\r
\r
    slotFilterToggle?.addEventListener('click', () => {\r
      if (!slotFilterSurface) return;\r
      const open = slotFilterSurface.classList.toggle('open');\r
      slotFilterToggle.setAttribute('aria-expanded', open ? 'true' : 'false');\r
    });\r
\r
    modalClose?.addEventListener('click', closeModal);\r
    slotModal?.addEventListener('click', (event) => {\r
      if (event.target === slotModal) {\r
        closeModal();\r
      }\r
    });\r
\r
    document.addEventListener('keydown', (event) => {\r
      if (event.key === 'Escape' && slotModal?.classList.contains('open')) {\r
        closeModal();\r
      }\r
    });\r
\r
    const init = async () => {\r
      const remoteCourts = await fetchCourtsFromApi();\r
      if (Array.isArray(remoteCourts) && remoteCourts.length) {\r
        courts = normalizeCourts(remoteCourts);\r
        courtCount = Math.max(...courts.map((court) => court.id));\r
      } else {\r
        courts = [createCourt(), createCourt()];\r
      }\r
      renderCourts();\r
      renderSlotFilterChips();\r
      renderSummary();\r
    };\r
\r
    void init();\r
  <\/script>\r
</body>\r
</html>\r
`,Fc="",zc=["innerHTML"],jn={__name:"LegacyPage",props:{file:{type:String,required:!0},styleId:{type:String,required:!0}},setup(n){const e=n,t=$o(""),o=Object.assign({"../../backup_old_files/club bookings.html":Bc,"../../backup_old_files/club home.html":Tc,"../../backup_old_files/club register.html":Lc,"../../backup_old_files/club.html":Pc,"../../backup_old_files/debug.html":Ac,"../../backup_old_files/home.html":Oc,"../../backup_old_files/index.html":Nc,"../../backup_old_files/join.html":Rc,"../../backup_old_files/login.html":Mc,"../../backup_old_files/onboarding-location.html":Dc,"../../backup_old_files/onboarding.html":Uc,"../../backup_old_files/user.html":jc,"../../backup_old_files/venue overview.html":Hc})[`../../backup_old_files/${e.file}`]||"",s=()=>{const p=o.match(/<body[^>]*>([\s\S]*?)<\/body>/i);return(p?p[1]:o).replace(/<script[\s\S]*?<\/script>/gi,"")},i=Object.assign({"../../backup_old_files/login-modal.js":Fc}),a=()=>[...o.matchAll(/<script\b([^>]*)>([\s\S]*?)<\/script>/gi)].map(m=>{const b=m[1]||"",_=m[2]||"",B=b.match(/src\s*=\s*["']([^"']+)["']/i);return{src:B?B[1]:null,code:_}}),l=()=>{a().forEach(m=>{let b=m.code;if(m.src){const U=`../../backup_old_files/${m.src.split("/").pop()}`;b=i[U]||""}if(!b)return;const _=document.createElement("script");_.type="text/javascript",_.textContent=b,document.body.appendChild(_)})},f=()=>[...o.matchAll(/<style[^>]*>([\s\S]*?)<\/style>/gi)].map(m=>m[1]).join(`
`),u=()=>{if(document.getElementById(e.styleId))return;const p=f();if(!p)return;const m=document.createElement("style");m.id=e.styleId,m.textContent=p,document.head.appendChild(m)};return os(()=>{t.value=s(),u(),setTimeout(()=>{l(),document.dispatchEvent(new Event("DOMContentLoaded"))},0)}),(p,m)=>(Es(),La("div",{class:"legacy-page",innerHTML:t.value},null,8,zc))}},$s=[{path:"/",component:jn,name:"Home",props:{file:"home.html",styleId:"legacy-home-styles"}},{path:"/login",component:jn,name:"Login",props:{file:"login.html",styleId:"legacy-login-styles"}},{path:"/join",component:jn,name:"Join",props:{file:"join.html",styleId:"legacy-join-styles"}},{path:"/user",component:jn,name:"User",props:{file:"user.html",styleId:"legacy-user-styles"}},{path:"/club",component:jn,name:"Club",props:{file:"club.html",styleId:"legacy-club-styles"}},{path:"/club-home",component:jn,name:"ClubHome",props:{file:"club home.html",styleId:"legacy-club-home-styles"}},{path:"/club-bookings",component:jn,name:"ClubBookings",props:{file:"club bookings.html",styleId:"legacy-club-bookings-styles"}},{path:"/venue-overview",component:jn,name:"VenueOverview",props:{file:"venue overview.html",styleId:"legacy-venue-overview-styles"}},{path:"/onboarding",component:jn,name:"Onboarding",props:{file:"onboarding.html",styleId:"legacy-onboarding-styles"}},{path:"/onboarding-location",component:jn,name:"OnboardingLocation",props:{file:"onboarding-location.html",styleId:"legacy-onboarding-location-styles"}},{path:"/:pathMatch(.*)*",redirect:"/"}],$c=kc({history:ec(),routes:$s}),Vs=gl(Sc);Vs.use($c);console.log("✅ App created");console.log("✅ Router configured with",$s.length,"routes");window.addEventListener("error",n=>{console.error("❌ Runtime error:",n.message,n.filename,n.lineno)});try{Vs.mount("#app"),console.log("✅ App mounted to #app")}catch(n){console.error("❌ Mount error:",n.message,n.stack),document.body.innerHTML=`<pre style="color:red">Error: ${n.message}
${n.stack}</pre>`}
