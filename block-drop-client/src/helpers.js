import { v4 as uuid } from 'uuid'

export const ROUTE_HOME = "/home"
export const ROUTE_ROOM = "/room"

export const generateHtmlId = () => `a${uuid().replace(/-/g, "")}`