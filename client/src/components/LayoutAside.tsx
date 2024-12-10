import { Link, matchPath } from "react-router-dom"
import { AsideNav } from "amis"
import store from "@/stores"
import React from "react"

const WorkStation = "/wms/workStation" // 工作站路由

const RenderAside = ({ navigations, iframeShow, iframeMenuClick }: any) => {
    function isActive(link: any, location: any) {
        if (location.pathname?.includes(WorkStation)) {
            return link?.includes(WorkStation)
        }
        const ret = matchPath(location.pathname, {
            path: link ? link.replace(/\?.*$/, "") : "",
            exact: true,
            strict: true
        })
        return !!ret
    }

    return (
        <AsideNav
            key={store.asideFolded ? "folded-aside" : "aside"}
            navigations={navigations}
            // style={{ background: "#1890ff" }}
            renderLink={({
                link,
                toggleExpand,
                classnames: cx,
                depth
            }: any) => {
                const isWorkStationCard = link.path === WorkStation
                const hasChildren = link.children?.length > 0
                if (
                    link.hidden ||
                    (!isWorkStationCard && link.path?.includes(WorkStation))
                ) {
                    return null
                }
                let children: any[] = []

                if (hasChildren && !isWorkStationCard) {
                    children.push(
                        <span
                            key="expand-toggle"
                            className={cx("AsideNav-itemArrow")}
                            onClick={(e) => toggleExpand(link, e)}
                        />
                    )
                }

                link.badge &&
                    children.push(
                        <b
                            key="badge"
                            className={cx(
                                `AsideNav-itemBadge`,
                                link.badgeClassName || "bg-info"
                            )}
                        >
                            {link.badge}
                        </b>
                    )

                if (store.asideFolded && link.icon) {
                    children.push(
                        <i
                            key="icon"
                            className={cx(`AsideNav-itemIcon`, link.icon)}
                            style={{ color: "#fff" }}
                        />
                    )
                } else if (store.asideFolded && depth === 1) {
                    children.push(
                        <i
                            key="icon"
                            className={cx(
                                `AsideNav-itemIcon`,
                                hasChildren ? "fa fa-folder" : "fa fa-info"
                            )}
                            style={{ color: "#fff" }}
                        />
                    )
                }

                children.push(
                    <span
                        key="label"
                        className={cx("AsideNav-itemLabel")}
                        style={{
                            color: "#fff"
                        }}
                    >
                        {link.languageValueMappings?.[store.locale] ||
                            link.title}
                    </span>
                )
                return !iframeShow && (isWorkStationCard || !hasChildren) ? (
                    <Link to={link.path}>{children}</Link>
                ) : (
                    <a
                        onClick={
                            iframeShow
                                ? (e) =>
                                      iframeMenuClick(
                                          link.path,
                                          link.permissions,
                                          e
                                      )
                                : hasChildren
                                ? (e) => toggleExpand(link, e)
                                : link.onClick
                        }
                    >
                        {children}
                    </a>
                )
            }}
            isActive={(link: any) =>
                isActive(iframeShow ? link.permissions : link.path, location)
            }
        />
    )
}

export default RenderAside
