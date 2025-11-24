# Pentaho Repository VFS Connection

This plugin provides a VFS connection to the repository. Whenever PDI is connected to the repository, there will be a PVFS connection named `Repository` that can read and write to it as the current user.

It also registers two underlying VFS connection schemes: `purr` for remote access to a repository from a client (Spoon/Pan/Kitchen/Standalone Carte), and `purl` for access to the local repository from within the server.

## Building

To build call from the main folder:

`mvn clean install`

There should be a zip under `assemblies/plugin/target` named `repo-vfs-plugin-assembly-<version>.zip`

### Deployment

To install the plugin just decompress the zip to PDI's plugins folder:

`unzip assemblies/plugin/target/repo-vfs-plugin-assembly-<version>.zip -d <PDI location>/plugins`

To install on the server, decompress it to the server PDI plugins folder:

`unzip assemblies/plugin/target/repo-vfs-plugin-assembly-<version>.zip -d <BI Server location>/pentaho-solutions/system/kettle/plugins`

## Modules

- **[`repo-vfs-ws/`](repo-vfs-ws/)** - Client-only implementation using BI Server web services, based on the `libpensol` implementation from Pentaho Report Designer.

- **[`repo-vfs-pur/`](repo-vfs-pur/)** - Implementations based on the `IUnifiedRepository` interface for both client (PDI) and server.

- **[`repo-vfs-pdi/`](repo-vfs-pdi/)** - PDI plugin infrastructure that registers PVFS and VFS providers.

- **[`server-vfs-test-plugin/`](server-vfs-test-plugin/)** - Development testing plugin. Pentaho server plugin that allows calling VFS operations for PVFS connections.  **Development use only** - not for production deployment
