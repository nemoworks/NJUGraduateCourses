%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%  Localization experiment main script
%  Nanjing University Dislab
%  Author: Wei Wang, Hujia Yu
%  Student: DZ1933026 Guochang Wang
%  Date: 2020/3/31
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% 

%% Step 1: Generate the simulation data
n=500; %number of nodes
x_size=10; %the width of the rectangular
y_size=10; %the length of the rectangular
range=1;   %communication range
anchor_num=5; % number of anchor nodes

% generate random (x,y) locations for sensor nodes
true_loc=generate_random_network(n,x_size,y_size);

% generate edgelist and node adjacency matrix
% the first two columns in edge list is the tail and head vertex id
% the thrid column is the distance of the edge
% adjacency matrix is a n*n sparse matrix
[edgelist,adjmatrix]=getedges(true_loc,range);

%get anchor nodes, there are anchor_num anchor nodes on each side of the
%rectangular, but actual number of anchors may change.
%the first column in anchor is the node id of the anchor, the next two
%columns are x and y coordinates
anchor=getanchor(true_loc,anchor_num);

% Draw the position and generated graph of the network
drawconnection(true_loc,edgelist,anchor);

fprintf('\n Showing the ture locations for sensors\n');
fprintf('Paused, press any key to continue or use Ctrl-C to stop\n');
pause;

%% Step 2: Graph Laplacian based Localization 
% You should only use the first two columns of the edge list and the
% anchor to find out est_loc

fprintf(1,'\n Starting Graph Laplacian based loalization\n');

iterations=5; % number of iterations
anchor_size=size(anchor,1); % actual number of anchors in the anchor set
edgeweight=ones(size(edgelist,1),1); % initialize edge weights to 1
tic
for loops= 1:iterations %iteratively sove the balanced srping network problem
    %You should write your own program to balance the spring network using
    %graph laplacian
    est_loc= balancenet(n,[edgelist(:,1:2) edgeweight],anchor);
    if(isnan(est_loc))
        fprintf('RCOND(Laplacian) too small to calculate\n');
        break
    end
    %You should write your own program to adjust the weight (stiffness) of
    %edges
    edgeweight=adjustweight(est_loc,[edgelist(:,1:2) edgeweight],range);
    
    % show results for each iteration
    drawconnection(est_loc,edgelist,anchor);
    fprintf('Iteration %d\n',loops);
    fprintf('Paused, press any key to continue or use Ctrl-C to stop\n');
    pause;
end
if(isnan(est_loc))
    fprintf('Paused, press any key to continue or use Ctrl-C to stop\n');
else
    running_time = toc;
    fprintf('Laplacian cost time: %f secends\n',running_time);
    pause;
    errors = (true_loc-est_loc).';
    average_loc_error = sum(sqrt(sum(errors.*errors)))/(n-size(anchor,1));
    fprintf('Laplacian average error: %f\n',average_loc_error);
    % show the final result
    compareresults(true_loc,est_loc,anchor);
    fprintf('\nFinal localization error for laplacian based method. \n');
    fprintf('Paused, press any key to continue or use Ctrl-C to stop\n');
    pause;
end

%You need to write your own code to calculate the average localization error
%the CDF of error distribution.

%% Step 3 MDS based localization
% % You should only use the first two columns of the edge list and the
% % anchor to find out est_loc

%use the edge list to get the relative position of sensors
%you need to write your own code for mds function.
tic
mds_loc=mds(n,edgelist(:,1:2));

if(isnan(mds_loc))
    fprintf('Graph not connected, mds does not work for the case \n');
    return
end

drawconnection(mds_loc,edgelist);

fprintf('\nRelative locations for MDS \n');
fprintf('Paused, press any key to continue or use Ctrl-C to stop\n');
pause;

% Do the scaling and rotation through linear regression
% put the estimated anchor location as the training data
training_data=[mds_loc(anchor(:,1),:) ones(size(anchor,1),1)];
% the output must be the ture location of the anchors
true_val=anchor(:,2:3);

theta=gradientdescent(training_data,true_val,0.01,1000);
est_loc=[mds_loc ones(n,1)]*theta;
runningtime = toc;
fprintf('mds cost time: %f secends\n',running_time);
errors = (true_loc-est_loc).';
average_loc_error = sum(sqrt(sum(errors.*errors)))/(n-size(anchor,1));
fprintf('mds average error: %f\n',average_loc_error);
drawconnection(est_loc,edgelist);
pause;
compareresults(true_loc,est_loc,anchor);