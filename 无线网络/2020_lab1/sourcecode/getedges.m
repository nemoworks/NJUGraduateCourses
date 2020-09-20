function [edgelist,adjmatrix]=getedges(nodexy,range)
% Function [edgelist,adjmatrix]=getedges(nodexy,range): generate a edge
% list and a node-node adjancency matrix for a given network and
% communication range
% Input: 
%   nodexy: the coordinates for the nodes
%   range: communication range of the network
%
% Output:
%   edgelist: the list of edges that appears in the network
%   adjmatrix: the sparse node-node adjancency matrix


n=size(nodexy,1); % get the number of nodes in the network

% calculate the distance between pairs of nodes
dismatrix=pdist2(nodexy,nodexy); 

% remove the edges that have distances longer than range
dismatrix=dismatrix.*(dismatrix<=range);

% find out the elements in the distance matrix with distance larger than 0
% edgelist(:,1) is the id of the tail node of the edge, 
% edgelist(:,2) is the id of the head node of the edge,
% edgelist(:,2) is the weight (distance) of the edge,
[edgelist(:,1),edgelist(:,2),edgelist(:,3)]=find( dismatrix );

% adjancency matrix is a sparse matrix that contain the weight of edge
% (i,j) at its (i,j) element
adjmatrix=sparse(edgelist(:,1),edgelist(:,2),edgelist(:,3),n,n);


end